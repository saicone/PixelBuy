package com.saicone.pixelbuy.core.data;

import com.saicone.delivery4j.AbstractMessenger;
import com.saicone.delivery4j.Broker;
import com.saicone.delivery4j.broker.HikariBroker;
import com.saicone.delivery4j.broker.RabbitMQBroker;
import com.saicone.delivery4j.broker.RedisBroker;
import com.saicone.delivery4j.util.DelayedExecutor;
import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.data.client.HikariDatabase;
import com.saicone.pixelbuy.module.data.delivery.BukkitExecutor;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Dependencies(value = {
        @Dependency("com.saicone.delivery4j:delivery4j:1.1.2"),
        @Dependency("com.saicone.delivery4j:broker-sql:1.1.2"),
        @Dependency(value = "com.saicone.delivery4j:broker-sql-hikari:1.1.2",
                relocate = {"com.zaxxer.hikari", "{package}.libs.hikari"}
        ),
        @Dependency(value = "com.saicone.delivery4j:broker-redis:1.1.2",
                relocate = {
                        "redis.clients.jedis", "{package}.libs.jedis",
                        "com.google.gson", "{package}.libs.gson",
                        "org.apache.commons.pool2", "{package}.libs.commons.pool2",
                        "org.json", "{package}.libs.json"
                }
        ),
        @Dependency(value = "com.saicone.delivery4j:broker-rabbitmq:1.1.2",
                relocate = {"com.rabbitmq", "{package}.libs.rabbitmq"}
        ),
        @Dependency(value = "com.saicone.delivery4j:extension-guava:1.1.2",
                transitive = false,
                relocate = {"com.google.common", "{package}.libs.guava"}
        ),
        @Dependency("org.slf4j:slf4j-nop:1.7.36")
}, relocations = {"com.saicone.delivery4j", "{package}.libs.delivery4j", "org.slf4j", "{package}.libs.slf4j"}
)
public class Messenger extends AbstractMessenger implements Broker.Logger {

    private final Database database;
    private final DelayedExecutor<?> executor;

    private String channel = "pixelbuy:main";

    private final Map<Long, CompletableFuture<Long>> ping = new HashMap<>();

    public Messenger(@NotNull Plugin plugin, @NotNull Database database) {
        this.database = database;
        this.executor = new BukkitExecutor(plugin);
        setExecutor(PixelBuy.get());
    }

    public void onLoad() {
        close();
        if (PixelBuy.settings().getIgnoreCase("messenger", "enabled").asBoolean(false)) {
            final String channel = PixelBuy.settings().getIgnoreCase("messenger", "channel").asString("pixelbuy:main");
            if (!channel.equals(this.channel)) {
                clear();
                setBroker(null);
                this.channel = channel;
            }
            if (!getChannels().containsKey(channel)) {
                subscribe(channel).consume((__, lines) -> {
                    PixelBuy.log(4, "Received messenger message: " + Arrays.toString(lines));
                    if (Bukkit.isPrimaryThread()) {
                        Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> process(lines));
                    } else {
                        process(lines);
                    }
                }).cache(true);
            }
            final Broker broker = loadBroker();
            broker.setExecutor(this.executor);
            start(broker);
        }
    }

    public void onDisable() {
        close();
        clear();
        setBroker(null);
    }

    @Override
    protected @NotNull Broker loadBroker() {
        String type = PixelBuy.settings().getIgnoreCase("messenger", "type").asString("AUTO");
        final String finalType;
        if (type.equalsIgnoreCase("AUTO")) {
            if (!"amqp://guest:guest@localhost:5672/%2F".equals(PixelBuy.settings().getIgnoreCase("messenger", "rabbitmq", "url").asString())
                    || PixelBuy.settings().getIgnoreCase("messenger", "rabbitmq", "host").getValue() != null) {
                finalType = "RABBITMQ";
            } else if (!"redis://:password@localhost:6379/0".equals(PixelBuy.settings().getIgnoreCase("messenger", "redis", "url").asString())
                    || PixelBuy.settings().getIgnoreCase("messenger", "redis", "host").getValue() != null) {
                finalType = "REDIS";
            } else if (this.database.getClient() instanceof HikariDatabase) {
                if (((HikariDatabase) this.database.getClient()).getType().isExternal()) {
                    finalType = "SQL";
                } else {
                    finalType = type;
                }
            } else {
                finalType = type;
            }
        } else {
            finalType = type;
        }

        PixelBuy.log(4, "Using delivery client: " + finalType);
        switch (finalType.toUpperCase()) {
            case "SQL":
                return loadHikariBroker((HikariDatabase) this.database.getClient());
            case "REDIS":
                return loadRedisBroker(PixelBuy.settings().getConfigurationSection(settings -> settings.getIgnoreCase("messenger", finalType)));
            case "RABBITMQ":
                return loadRabbitMQBroker(PixelBuy.settings().getConfigurationSection(settings -> settings.getIgnoreCase("messenger", finalType)));
            default:
                break;
        }
        throw new IllegalArgumentException("The messenger type '" + finalType + "' is not a valid type");
    }

    @NotNull
    private Broker loadHikariBroker(@NotNull HikariDatabase database) {
        if (!database.getType().isExternal()) {
            throw new IllegalArgumentException("The current SQL database is not an external database type");
        }
        final HikariBroker broker = new HikariBroker(database.getHikari());
        broker.setTablePrefix(database.getPrefix());
        return broker;
    }

    @NotNull
    private Broker loadRedisBroker(@Nullable BukkitSettings config) {
        Objects.requireNonNull(config, "Cannot find Redis configuration");
        final String url = config.getIgnoreCase("url").asString();
        if (url == null) {
            final String host = config.getIgnoreCase("host").asString("localhost");
            final int port = config.getIgnoreCase("port").asInt(6379);
            final String password = config.getIgnoreCase("password").asString("password");
            final int database = config.getIgnoreCase("database").asInt(0);
            final boolean ssl = config.getIgnoreCase("ssl").asBoolean(false);
            return RedisBroker.of(host, port, password, database, ssl);
        } else {
            return RedisBroker.of(url);
        }
    }

    @NotNull
    private Broker loadRabbitMQBroker(@Nullable BukkitSettings config) {
        Objects.requireNonNull(config, "Cannot find RabbitMQ configuration");
        final String exchange = config.getIgnoreCase("exchange").asString("pixelbuy");
        final String url = config.getIgnoreCase("url").asString();
        if (url == null) {
            final String host = config.getIgnoreCase("host").asString("localhost");
            final int port = config.getIgnoreCase("port").asInt(6379);
            final String username = config.getIgnoreCase("username").asString("guest");
            final String password = config.getIgnoreCase("password").asString("guest");
            final String virtualHost = config.getIgnoreCase("virtualhost").asString("%2F");
            return RabbitMQBroker.of(host, port, username, password, virtualHost, exchange);
        } else {
            return RabbitMQBroker.of(url, exchange);
        }
    }

    @Override
    public void log(int level, @NotNull String msg) {
        PixelBuy.log(level, msg);
    }

    @Override
    public void log(int level, @NotNull String msg, @NotNull Throwable throwable) {
        PixelBuy.logException(level, throwable, msg);
    }

    @NotNull
    public CompletableFuture<Long> ping() {
        final Long key = System.currentTimeMillis();
        final CompletableFuture<Long> future = new CompletableFuture<Long>().completeOnTimeout(Long.MIN_VALUE, 20, TimeUnit.SECONDS);
        ping.put(key, future);
        send(channel, "PING", key);
        return future;
    }

    public void process(@NotNull StoreUser user, @NotNull String group) {
        send(channel, "PROCESS_USER", user.getUniqueId(), group);
    }

    public void update(@NotNull StoreUser user) {
        send(channel, "UPDATE_DONATED", user.getUniqueId(), user.getDonated());
    }

    public void update(@NotNull StoreOrder order) {
        send(channel, "UPDATE_ORDER", order.getBuyer(), order.getProvider(), order.getId(), order.getGroup());
    }

    public void delete(@NotNull StoreOrder order) {
        send(channel, "DELETE_ORDER", order.getBuyer(), order.getProvider(), order.getId(), order.getGroup());
    }

    private void process(@NotNull String[] lines) {
        if (lines.length < 3) {
            if (lines.length == 2) {
                if (lines[0].equalsIgnoreCase("PING")) {
                    send(channel, "PONG", lines[1]);
                } else if (lines[0].equalsIgnoreCase("PONG")) {
                    final long key = Long.parseLong(lines[1]);
                    final long time = System.currentTimeMillis() - key;
                    final CompletableFuture<Long> future = ping.remove(key);
                    if (future != null) {
                        future.complete(time);
                    }
                }
            }
            return;
        }
        try {
            final StoreUser user = database.getCached(UUID.fromString(lines[1]));
            if (user == null) {
                return;
            }
            switch (lines[0].toUpperCase()) {
                case "PROCESS_USER":
                    if (PixelBuy.get().getStore().getGroup().equals(lines[2]) && Bukkit.getPlayer(user.getUniqueId()) != null) {
                        PixelBuy.get().getStore().getCheckout().process(user);
                    }
                    break;
                case "UPDATE_DONATED":
                    user.setDonated(Float.parseFloat(lines[2]));
                    break;
                case "UPDATE_ORDER":
                    if (user.isLoaded()) {
                        final StoreOrder order = database.getClient().getOrder(lines[2], Integer.parseInt(lines[3]), lines[4]);
                        if (order != null) {
                            user.updateOrder(order);
                        }
                    }
                    break;
                case "DELETE_ORDER":
                    if (user.getOrders().isEmpty()) {
                        break;
                    }
                    user.removeOrder(lines[2], Integer.parseInt(lines[3]), lines[4]);
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            PixelBuy.logException(2, t, "Error while reading delivery message");
        }
    }
}
