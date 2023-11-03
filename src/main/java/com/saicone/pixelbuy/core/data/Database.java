package com.saicone.pixelbuy.core.data;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.module.data.client.HikariDatabase;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.data.DataClient;

import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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
    }

    public void onDisable() {
        if (client != null) {
            client.saveUsers(cached.values());
            client.onClose();
        }
    }

    public void onReload() {
        if (client != null) {
            client.saveUsers(cached.values());
            client.onClose();
        }
        onLoad();
    }

    @NotNull
    public Map<UUID, StoreUser> getCached() {
        return cached;
    }

    @Nullable
    public StoreUser getCached(@NotNull UUID uniqueId) {
        return cached.get(uniqueId);
    }

    @Nullable
    public UUID getUniqueId(@NotNull String username) {
        for (var entry : cached.entrySet()) {
            if (username.equalsIgnoreCase(entry.getValue().getName())) {
                return entry.getValue().getUniqueId();
            }
        }
        return null;
    }

    @NotNull
    public DataClient getClient() {
        return client;
    }

    @NotNull
    public StoreUser getData(@NotNull UUID uniqueId, @NotNull String username) {
        final StoreUser user = cached.get(uniqueId);
        if (user != null) {
            return user;
        }
        client.getUser(uniqueId, username, loaded -> {
            if (loaded == null) {
                loaded = new StoreUser(uniqueId, username, 0.0f);
            }
            loaded.setLoaded(true);
            client.getOrders(uniqueId, loaded::addOrder);
            cached.put(uniqueId, loaded);
        });
        return cached.get(uniqueId);
    }

    @NotNull
    public StoreUser getDataAsync(@NotNull UUID uniqueId, @NotNull String username) {
        final StoreUser cachedUser = cached.get(uniqueId);
        if (cachedUser != null) {
            return cachedUser;
        }
        cached.put(uniqueId, new StoreUser(uniqueId, username, 0.0f));
        client.getUserAsync(uniqueId, username, user -> {
            if (user == null) {
                user = new StoreUser(uniqueId, username, 0.0f);
            }
            StoreUser foundUser = cached.get(user.getUniqueId());
            if (foundUser == null) {
                cached.put(uniqueId, user);
                foundUser = user;
            } else {
                foundUser.addDonated(user.getDonated());
            }
            loadOrders(true, foundUser);
        });
        return cached.get(uniqueId);
    }

    @Nullable
    public StoreUser getDataOrNull(@NotNull UUID uniqueId, @NotNull String username) {
        final StoreUser user = cached.get(uniqueId);
        if (user != null) {
            return user;
        }
        client.getUser(uniqueId, username, loaded -> {
            if (loaded != null) {
                loaded.setLoaded(true);
                client.getOrders(uniqueId, loaded::addOrder);
                cached.put(uniqueId, loaded);
            }
        });
        return cached.get(uniqueId);
    }

    public void loadOrders(boolean sync, @NotNull StoreUser user) {
        user.setLoaded(true);
        client.getOrders(sync, user.getUniqueId(), user::mergeOrder);
    }

    public void loadUser(boolean sync, @NotNull UUID uniqueId, @NotNull String username, @NotNull Consumer<StoreUser> consumer) {
        final StoreUser cachedUser = cached.get(uniqueId);
        if (cachedUser != null) {
            if (!cachedUser.isLoaded()) {
                loadOrders(sync, cachedUser);
            }
            consumer.accept(cachedUser);
            return;
        }
        if (!sync) {
            // Temp value
            cached.put(uniqueId, new StoreUser(uniqueId, username, 0.0f));
        }
        client.getUser(sync, uniqueId, username, user -> {
            if (user == null) {
                user = new StoreUser(uniqueId, username, 0.0f);
            }
            StoreUser foundUser = cached.get(user.getUniqueId());
            if (foundUser == null) {
                cached.put(uniqueId, user);
                foundUser = user;
            } else {
                foundUser.addDonated(user.getDonated());
            }
            loadOrders(true, foundUser);
            consumer.accept(foundUser);
        });
    }

    public void loadUsers(boolean sync) {
        client.getUsers(sync, user -> cached.put(user.getUniqueId(), user));
    }

    public void saveDataAsync(@Nullable StoreUser user) {
        if (user != null) {
            client.saveUserAsync(user);
        }
    }

    public void saveDataAsync(@Nullable StoreOrder order) {
        if (order != null) {
            client.saveOrdersAsync(Collections.singleton(order));
        }
    }
}
