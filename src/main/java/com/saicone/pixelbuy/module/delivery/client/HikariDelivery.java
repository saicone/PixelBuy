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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HikariDelivery extends DeliveryClient {

    private final HikariDataSource hikari;
    private final String tablePrefix;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private long currentID = -1;
    private Thread getThread = null;
    private Thread cleanThread = null;

    @NotNull
    public static HikariDelivery of(@NotNull String url, @NotNull String username, @NotNull String password, @NotNull String tablePrefix) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return new HikariDelivery(new HikariDataSource(config), tablePrefix);
    }

    public HikariDelivery(@NotNull HikariDataSource hikari, @NotNull String tablePrefix) {
        this.hikari = hikari;
        this.tablePrefix = tablePrefix;
    }

    @Override
    public void onStart() {
        try (Connection connection = hikari.getConnection()) {
            // Taken from LuckPerms
            String createTable = "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "messenger` (`id` INT AUTO_INCREMENT NOT NULL, `time` TIMESTAMP NOT NULL, `channel` VARCHAR(255) NOT NULL, `msg` TEXT NOT NULL, PRIMARY KEY (`id`)) DEFAULT CHARSET = utf8mb4";
            try (Statement statement = connection.createStatement()) {
                try {
                    statement.execute(createTable);
                } catch (SQLException e) {
                    if (e.getMessage().contains("Unknown character set")) {
                        statement.execute(createTable.replace("utf8mb4", "utf8"));
                    } else {
                        throw e;
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("SELECT MAX(`id`) as `latest` FROM `" + tablePrefix + "messenger`")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        currentID = resultSet.getLong("latest");
                    }
                }
            }
            enabled = true;
            if (getThread == null) {
                getThread = delayThread(this::getMessages, 1000);
            }
            if (cleanThread == null) {
                cleanThread = delayThread(this::cleanMessages, 30000);
            }
            getThread.start();
            cleanThread.start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose() {
        hikari.close();
        getThread.interrupt();
        cleanThread.interrupt();
    }

    @Override
    public void onSend(@NotNull String channel, byte[] data) {
        if (!enabled) {
            return;
        }
        lock.readLock().lock();

        try (Connection connection = hikari.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + tablePrefix + "messenger` (`time`, `channel`, `msg`) VALUES(NOW(), ?, ?)")) {
                statement.setString(1, channel);
                statement.setString(2, toBase64(data));
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lock.readLock().unlock();
    }

    @NotNull
    public HikariDataSource getHikari() {
        return hikari;
    }

    public void getMessages() {
        if (!enabled) {
            return;
        }
        lock.readLock().lock();

        try (Connection connection = hikari.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT `id`, `channel`, `msg` FROM `" + tablePrefix + "messenger` WHERE `id` > ? AND (NOW() - `time` < 30)")) {
                statement.setLong(1, currentID);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong("id");
                        currentID = Math.max(currentID, id);

                        String channel = rs.getString("channel");
                        String message = rs.getString("msg");
                        if (subscribedChannels.contains(channel) && message != null) {
                            receive(channel, fromBase64(message));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lock.readLock().unlock();
    }

    public void cleanMessages() {
        if (!enabled) {
            return;
        }
        lock.readLock().lock();

        try (Connection connection = hikari.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + tablePrefix + "messenger` WHERE (NOW() - `time` > 60)")) {
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lock.readLock().unlock();
    }

    @NotNull
    @SuppressWarnings("all")
    private Thread delayThread(@NotNull Runnable runnable, long millis) {
        return new Thread(() -> {
            while (enabled && !Thread.interrupted() && hikari.isRunning()) {
                runnable.run();
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
