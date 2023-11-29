package com.saicone.pixelbuy.core.data;

import com.saicone.ezlib.EzlibLoader.Dependency;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.data.client.HikariDatabase;
import com.saicone.pixelbuy.module.delivery.AbstractMessenger;
import com.saicone.pixelbuy.module.delivery.DeliveryClient;
import com.saicone.pixelbuy.module.delivery.client.HikariDelivery;
import com.saicone.pixelbuy.module.delivery.client.RabbitMQDelivery;
import com.saicone.pixelbuy.module.delivery.client.RedisDelivery;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class Messenger extends AbstractMessenger {

    private static final Dependency SLF4J_DEPENDENCY = new Dependency()
            .path("org.slf4j:slf4j-nop:1.7.36")
            .relocate("org.slf4j", "{package}.libs.slf4j");
    private static final Dependency REDIS_DEPENDENCY = new Dependency()
            .path("redis{}clients:jedis:4.4.6")
            .relocate(
                    "redis{}clients{}jedis", "{package}.libs.jedis",
                    "com.google.gson", "{package}.libs.gson",
                    "org.apache.commons.pool2", "{package}.libs.commons.pool2",
                    "org.json", "{package}.libs.json",
                    "org.slf4j", "{package}.libs.slf4j"
            );
    private static final Dependency RABBITMQ_DEPENDENCY = new Dependency()
            .path("com{}rabbitmq:amqp-client:5.20.0")
            .relocate(
                    "com{}rabbitmq", "{package}.libs.rabbitmq",
                    "org.slf4j", "{package}.libs.slf4j"
            );

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
                subscribe(channel, (msg) -> {
                    if (Bukkit.isPrimaryThread()) {
                        Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> process(msg));
                    } else {
                        process(msg);
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
    private HikariDelivery loadHikariDelivery(@NotNull HikariDatabase database) {
        if (!database.getType().isExternal()) {
            throw new IllegalArgumentException("The current SQL database is not an external database type");
        }
        return new HikariDelivery(database.getHikari(), database.getPrefix());
    }

    @NotNull
    private RedisDelivery loadRedisDelivery(@Nullable BukkitSettings config) {
        Objects.requireNonNull(config, "Cannot find Redis configuration");
        PixelBuy.get().getLibraryLoader().applyDependency(SLF4J_DEPENDENCY);
        PixelBuy.get().getLibraryLoader().applyDependency(REDIS_DEPENDENCY);
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
    private RabbitMQDelivery loadRabbitMQDelivery(@Nullable BukkitSettings config) {
        Objects.requireNonNull(config, "Cannot find RabbitMQ configuration");
        PixelBuy.get().getLibraryLoader().applyDependency(SLF4J_DEPENDENCY);
        PixelBuy.get().getLibraryLoader().applyDependency(RABBITMQ_DEPENDENCY);
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
    protected void log(int level, @NotNull String msg) {
        PixelBuy.log(level, msg);
    }

    public void process(@NotNull StoreUser user) {
        send(channel, "PROCESS_USER|" + user.getUniqueId());
    }

    public void update(@NotNull StoreUser user) {
        send(channel, "UPDATE_DONATED|" + user.getUniqueId() + "|" + user.getDonated());
    }

    public void update(@NotNull StoreOrder order) {
        send(channel, "UPDATE_ORDER|" + order.getBuyer() + "|" + order.getProvider() + "|" + order.getId() + "|" + order.getGroup());
    }

    public void delete(@NotNull StoreOrder order) {
        send(channel, "DELETE_ORDER|" + order.getBuyer() + "|" + order.getProvider() + "|" + order.getId() + "|" + order.getGroup());
    }

    private void process(@NotNull String message) {
        final String[] split = message.split("[|]");
        PixelBuy.log(4, "Received message: " + message);
        if (split.length < 2) {
            return;
        }
        try {
            final StoreUser user = database.getCached(UUID.fromString(split[1]));
            if (user == null) {
                return;
            }
            switch (split[0].toUpperCase()) {
                case "PROCESS_USER":
                    if (Bukkit.getPlayer(user.getUniqueId()) != null) {
                        PixelBuy.get().getStore().getCheckout().process(user);
                    }
                    break;
                case "UPDATE_DONATED":
                    user.setDonated(Float.parseFloat(split[2]));
                    break;
                case "UPDATE_ORDER":
                    if (user.isLoaded()) {
                        database.getClient().getOrder(split[2], Integer.parseInt(split[3]), split[4], user::updateOrder);
                    }
                    break;
                case "DELETE_ORDER":
                    if (user.getOrders().isEmpty()) {
                        break;
                    }
                    user.removeOrder(split[2], Integer.parseInt(split[3]), split[4]);
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            PixelBuy.logException(2, t, "Error while reading delivery message");
        }
    }
}
