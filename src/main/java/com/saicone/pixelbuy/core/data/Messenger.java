package com.saicone.pixelbuy.core.data;

import com.saicone.delivery4j.AbstractMessenger;
import com.saicone.delivery4j.DeliveryClient;
import com.saicone.delivery4j.client.HikariDelivery;
import com.saicone.delivery4j.client.RabbitMQDelivery;
import com.saicone.delivery4j.client.RedisDelivery;
import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.data.client.HikariDatabase;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Dependencies(value = {
        @Dependency("com.saicone.delivery4j:delivery4j:1.0"),
        @Dependency(value = "com.saicone.delivery4j:delivery4j-hikari:1.0",
                transitive = false,
                relocate = {"com.zaxxer.hikari", "{package}.libs.hikari"}
        ),
        @Dependency(value = "com.saicone.delivery4j:delivery4j-redis:1.0",
                relocate = {
                        "redis.clients.jedis", "{package}.libs.jedis",
                        "com.google.gson", "{package}.libs.gson",
                        "org.apache.commons.pool2", "{package}.libs.commons.pool2",
                        "org.json", "{package}.libs.json"
                }
        ),
        @Dependency(value = "com.saicone.delivery4j:delivery4j-rabbitmq:1.0",
                relocate = {"com.rabbitmq", "{package}.libs.rabbitmq"}
        ),
        @Dependency("org.slf4j:slf4j-nop:1.7.36")
}, relocations = {"com.saicone.delivery4j", "{package}.libs.delivery4j", "org.slf4j", "{package}.libs.slf4j"}
)
public class Messenger extends AbstractMessenger {

    private final Database database;

    private String channel = "pixelbuy:main";

    public Messenger(@NotNull Database database) {
        this.database = database;
    }

    public void onLoad() {
        close();
        if (PixelBuy.settings().getIgnoreCase("messenger", "enabled").asBoolean(false)) {
            final String channel = PixelBuy.settings().getIgnoreCase("messenger", "channel").asString("pixelbuy:main");
            if (!channel.equals(this.channel)) {
                clear();
                this.deliveryClient = null;
                this.channel = channel;
            }
            if (!incomingConsumers.containsKey(channel)) {
                subscribe(channel, (lines) -> {
                    PixelBuy.log(4, "Received messenger message: " + Arrays.toString(lines));
                    if (Bukkit.isPrimaryThread()) {
                        Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> process(lines));
                    } else {
                        process(lines);
                    }
                });
            }
            start();
        }
    }

    public void onDisable() {
        close();
        clear();
    }

    @Override
    protected @NotNull DeliveryClient loadDeliveryClient() {
        String type = PixelBuy.settings().getIgnoreCase("messenger", "type").asString("AUTO");
        final String finalType;
        if (type.equalsIgnoreCase("AUTO")) {
            if (!"amqp://guest:guest@localhost:5672/%2F".equals(PixelBuy.settings().getIgnoreCase("messenger", "rabbitmq", "url").asString())
                    || PixelBuy.settings().getIgnoreCase("messenger", "rabbitmq", "host").getValue() != null) {
                finalType = "RABBITMQ";
            } else if (!"redis://:password@localhost:6379/0".equals(PixelBuy.settings().getIgnoreCase("messenger", "redis", "url").asString())
                    || PixelBuy.settings().getIgnoreCase("messenger", "redis", "host").getValue() != null) {
                finalType = "REDIS";
            } else if (PixelBuy.get().getDatabase().getClient() instanceof HikariDatabase) {
                if (((HikariDatabase) PixelBuy.get().getDatabase().getClient()).getType().isExternal()) {
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
                return loadHikariDelivery((HikariDatabase) PixelBuy.get().getDatabase().getClient());
            case "REDIS":
                return loadRedisDelivery(PixelBuy.settings().getConfigurationSection(settings -> settings.getIgnoreCase("messenger", finalType)));
            case "RABBITMQ":
                return loadRabbitMQDelivery(PixelBuy.settings().getConfigurationSection(settings -> settings.getIgnoreCase("messenger", finalType)));
            default:
                break;
        }
        throw new IllegalArgumentException("The messenger type '" + finalType + "' is not a valid type");
    }

    @NotNull
    private DeliveryClient loadHikariDelivery(@NotNull HikariDatabase database) {
        if (!database.getType().isExternal()) {
            throw new IllegalArgumentException("The current SQL database is not an external database type");
        }
        return new HikariDelivery(database.getHikari(), database.getPrefix());
    }

    @NotNull
    private DeliveryClient loadRedisDelivery(@Nullable BukkitSettings config) {
        Objects.requireNonNull(config, "Cannot find Redis configuration");
        final String url = config.getIgnoreCase("url").asString();
        if (url == null) {
            final String host = config.getIgnoreCase("host").asString("localhost");
            final int port = config.getIgnoreCase("port").asInt(6379);
            final String password = config.getIgnoreCase("password").asString("password");
            final int database = config.getIgnoreCase("database").asInt(0);
            final boolean ssl = config.getIgnoreCase("ssl").asBoolean(false);
            return RedisDelivery.of(host, port, password, database, ssl);
        } else {
            return RedisDelivery.of(url);
        }
    }

    @NotNull
    private DeliveryClient loadRabbitMQDelivery(@Nullable BukkitSettings config) {
        Objects.requireNonNull(config, "Cannot find RabbitMQ configuration");
        final String exchange = config.getIgnoreCase("exchange").asString("pixelbuy");
        final String url = config.getIgnoreCase("url").asString();
        if (url == null) {
            final String host = config.getIgnoreCase("host").asString("localhost");
            final int port = config.getIgnoreCase("port").asInt(6379);
            final String username = config.getIgnoreCase("username").asString("guest");
            final String password = config.getIgnoreCase("password").asString("guest");
            final String virtualHost = config.getIgnoreCase("virtualhost").asString("%2F");
            return RabbitMQDelivery.of(host, port, username, password, virtualHost, exchange);
        } else {
            return RabbitMQDelivery.of(url, exchange);
        }
    }

    @Override
    public void log(int level, @NotNull Throwable t) {
        PixelBuy.logException(level, t);
    }

    @Override
    public void log(int level, @NotNull String msg) {
        PixelBuy.log(level, msg);
    }

    @Override
    public @NotNull Runnable async(@NotNull Runnable runnable) {
        final BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), runnable);
        return task::cancel;
    }

    @Override
    public @NotNull Runnable asyncRepeating(@NotNull Runnable runnable, long time, @NotNull TimeUnit unit) {
        final long ticks = unit.toMillis(time) / 50;
        final BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(PixelBuy.get(), runnable, ticks, ticks);
        return task::cancel;
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
                        database.getClient().getOrder(lines[2], Integer.parseInt(lines[3]), lines[4], user::updateOrder);
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
