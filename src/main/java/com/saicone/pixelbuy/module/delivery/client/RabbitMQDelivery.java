/*
 * This file is part of mcode, licensed under the MIT License
 *
 * Copyright (c) Rubenicos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.saicone.pixelbuy.module.delivery.client;

import com.rabbitmq.client.*;
import com.saicone.pixelbuy.module.delivery.DeliveryClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;

public class RabbitMQDelivery extends DeliveryClient {

    private final Connection connection;
    private final String exchange;

    private Channel cChannel = null;
    private String queue = null;

    private boolean reconnected = false;

    @NotNull
    public static RabbitMQDelivery of(@NotNull String url, @NotNull String exchange) {
        try {
            return of(new URI(url), exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static RabbitMQDelivery of(@NotNull URI uri, @NotNull String exchange) {
        final ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(uri);
            return new RabbitMQDelivery(factory.newConnection(), exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static RabbitMQDelivery of(@NotNull String host, int port, @NotNull String username, @NotNull String password, @NotNull String virtualHost, @NotNull String exchange) {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        try {
            return new RabbitMQDelivery(factory.newConnection(), exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RabbitMQDelivery(@NotNull Connection connection, @NotNull String exchange) {
        this.connection = connection;
        this.exchange = exchange;
    }

    @Override
    public void onStart() {
        // Random stuff, let's go!
        new Thread(() -> {
            try {
                // First, create a channel
                cChannel = connection.createChannel();

                // Second!
                // Create auto-delete queue
                this.queue = cChannel.queueDeclare(queue, false, true, true, null).getQueue();
                // With auto-delete pre-channel (exchange in RabbitMQ)
                cChannel.exchangeDeclare(exchange, BuiltinExchangeType.TOPIC, false, true, null);
                // And subscribed channels (routing keys in RabbitMQ)
                for (String channel : subscribedChannels) {
                    cChannel.queueBind(this.queue, exchange, channel);
                }

                // Third, and most important
                // Register callback for message delivery
                cChannel.basicConsume(this.queue, true, (consumerTag, message) -> {
                    final String channel = message.getEnvelope().getRoutingKey();
                    if (subscribedChannels.contains(channel)) {
                        receive(channel, message.getBody());
                    }
                }, __ -> {}); // Without canceled delivery

                if (reconnected) {
                    log(3, "RabbitMQ connection is alive again");
                    reconnected = false;
                }
                enabled = true;
            } catch (Throwable t) {
                t.printStackTrace();
                return;
            }

            // Maintain the connection alive
            alive();
        }).start();
    }

    @Override
    public void onClose() {
        close(cChannel, connection);
        cChannel = null;
    }

    @Override
    public void onSubscribe(@NotNull String... channels) {
        for (String channel : channels) {
            try {
                cChannel.queueBind(queue, exchange, channel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUnsubscribe(@NotNull String... channels) {
        for (String channel : channels) {
            try {
                cChannel.queueUnbind(queue, exchange, channel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSend(@NotNull String channel, byte[] data) {
        if (cChannel == null) {
            return;
        }

        try {
            // Publish to exchange and routing key without any special properties
            cChannel.basicPublish(exchange, channel, new AMQP.BasicProperties.Builder().build(), data);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }

    @SuppressWarnings("all")
    private void alive() {
        while (enabled && !Thread.interrupted()) {
            if (connection != null && connection.isOpen() && cChannel != null && cChannel.isOpen()) {
                try {
                    Thread.sleep(30_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                log(2, "RabbitMQ connection dropped, automatic reconnection every 8 seconds...");
                onClose();

                reconnected = true;
                onStart();

                if (!enabled) {
                    enabled = true;
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void close(AutoCloseable... closeables) {
        try {
            for (AutoCloseable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
