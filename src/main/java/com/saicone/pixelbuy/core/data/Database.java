package com.saicone.pixelbuy.core.data;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.module.data.client.HikariDatabase;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.data.DataClient;

import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Database {

    private final Map<UUID, StoreUser> cached = new ConcurrentHashMap<>();

    private DataClient client;

    public void onLoad() {
        final String type = PixelBuy.settings().getIgnoreCase("database", "type").asString("SQL");
        if (!type.equalsIgnoreCase("SQL")) {
            PixelBuy.log(1, "The database type '" + type + "' doesn't exist");
            return;
        }

        final BukkitSettings config = PixelBuy.settings().getConfigurationSection(settings -> settings.getIgnoreCase("database", type));
        if (config == null) {
            PixelBuy.log(1, "Cannot find configuration for database type: " + type);
            return;
        }

        client = new HikariDatabase();
        client.onLoad(config);
        client.onStart();

        if (PixelBuy.settings().getIgnoreCase("database", "loadall").asBoolean(true)) {
            client.getUsers(user -> cached.put(user.getUniqueId(), user));
        }
    }

    public void onDisable() {
        if (client != null) {
            client.saveUsers(cached.values());
            client.onClose();
        }
    }

    @NotNull
    public Map<UUID, StoreUser> getCached() {
        return cached;
    }

    @NotNull
    public DataClient getClient() {
        return client;
    }

    public void saveData(@Nullable StoreUser user) {
        if (user != null) {
            client.saveUser(user);
        }
    }

    @Nullable
    public StoreUser getData(@NotNull UUID uniqueId, @NotNull String username) {
        return cached.computeIfAbsent(uniqueId, id -> {
            client.getUser(uniqueId, username, user -> {
                cached.put(uniqueId, user);
                client.getOrders(uniqueId, user::addOrder);
            });
            return new StoreUser(uniqueId, username, 0.0f);
        });
    }

}
