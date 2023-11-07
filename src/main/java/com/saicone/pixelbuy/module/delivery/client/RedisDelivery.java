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

import com.saicone.pixelbuy.module.delivery.DeliveryClient;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisDataException;

import java.net.URI;

public class RedisDelivery extends DeliveryClient {

    private final JedisPool pool;
    private final String password;
    private final Bridge bridge;

    private Thread thread = null;

    @NotNull
    public static RedisDelivery of(@NotNull String url) {
        String password = "";
        if (url.contains("@")) {
            final String s = url.substring(0, url.lastIndexOf("@"));
            if (s.contains(":")) {
                password = s.substring(s.lastIndexOf(":") + 1);
            }
        }
        try {
            return of(new URI(url), password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static RedisDelivery of(@NotNull URI uri, @NotNull String password) {
        return new RedisDelivery(new JedisPool(uri), password);
    }

    @NotNull
    public static RedisDelivery of(@NotNull String host, int port, @NotNull String password, int database, boolean ssl) {
        return new RedisDelivery(new JedisPool(new JedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, password, database, ssl), password);
    }

    public RedisDelivery(@NotNull JedisPool pool, @NotNull String password) {
        this.pool = pool;
        this.password = password;
        this.bridge = new Bridge();
    }

    public RedisDelivery(@NotNull JedisPool pool, @NotNull String password, @NotNull Bridge bridge) {
        this.pool = pool;
        this.password = password;
        this.bridge = bridge;
    }

    @Override
    public void onStart() {
        enabled = true;
        // Jedis connection is a blocking operation.
        // So new thread is needed to not block the main thread
        thread = new Thread(this::alive);
        thread.start();
    }

    @Override
    public void onClose() {
        enabled = false;
        try {
            bridge.unsubscribe();
        } catch (Throwable ignored) { }
        pool.destroy();
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void onSubscribe(@NotNull String... channels) {
        try {
            bridge.unsubscribe();
        } catch (Throwable ignored) { }
        if (thread != null) {
            thread.interrupt();
            thread.start();
        }
    }

    @Override
    public void onUnsubscribe(@NotNull String... channels) {
        try {
            bridge.unsubscribe();
        } catch (Throwable ignored) { }
        if (thread != null) {
            thread.interrupt();
            thread.start();
        }
    }

    @Override
    public void onSend(@NotNull String channel, byte[] data) {
        try (Jedis jedis = pool.getResource()) {
            final String message = toBase64(data);
            try {
                jedis.publish(channel, message);
            } catch (JedisDataException e) {
                // Fix Java +16 error
                if (e.getMessage().contains("NOAUTH")) {
                    jedis.auth(password);
                    jedis.publish(channel, message);
                } else {
                    throw e;
                }
            }
        }
    }

    @NotNull
    public JedisPool getPool() {
        return pool;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    @NotNull
    public Bridge getBridge() {
        return bridge;
    }

    @SuppressWarnings("all")
    private void alive() {
        boolean reconnected = false;
        while (enabled && !Thread.interrupted() && pool != null && !pool.isClosed()) {
            try (Jedis jedis = pool.getResource()) {
                if (reconnected) {
                    log(3, "Redis connection is alive again");
                }
                // Subscribe channels and lock the thread
                jedis.subscribe(bridge, subscribedChannels.toArray(new String[0]));
            } catch (Throwable t) {
                // Thread was unlocked due error
                if (enabled) {
                    if (reconnected) {
                        log(2, "Redis connection dropped, automatic reconnection in 8 seconds...\n" + t.getMessage());
                    }
                    try {
                        bridge.unsubscribe();
                    } catch (Throwable ignored) { }

                    // Make an instant subscribe if ocurrs any error on initialization
                    if (!reconnected) {
                        reconnected = true;
                    } else {
                        try {
                            Thread.sleep(8000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }

    public class Bridge extends JedisPubSub {

        @Override
        public void onMessage(String channel, String message) {
            if (channel != null && subscribedChannels.contains(channel) && message != null) {
                receive(channel, fromBase64(message));
            }
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            log(3, "Redis subscribed to channel '" + channel + "'");
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            log(3, "Redis unsubscribed from channel '" + channel + "'");
        }
    }
}
