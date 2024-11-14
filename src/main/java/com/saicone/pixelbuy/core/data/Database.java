package com.saicone.pixelbuy.core.data;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.module.data.client.HikariDatabase;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.data.DataClient;

import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class Database implements Listener, Executor {

    private final Map<UUID, StoreUser> cached = new ConcurrentHashMap<>();
    private List<UUID> sorted = List.of();
    private final Messenger messenger;

    private boolean userLoadAll = true;
    private long topLimit = -1;
    private long topTime = -1;

    private int topTask = -1;
    private DataClient client;
    private boolean registered;

    public Database() {
        this.messenger = new Messenger(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final UUID uniqueId = event.getPlayer().getUniqueId();
        final StoreUser cachedUser = cached.get(uniqueId);
        if (cachedUser == null) {
            // Add temp value
            cached.put(uniqueId, new StoreUser(uniqueId, event.getPlayer().getName(), 0.0f));
        }
        execute(() -> loadUser(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        saveDataAsync(getCached(event.getPlayer().getUniqueId())).thenAccept(this::unloadUser);
    }

    public void onLoad() {
        userLoadAll = PixelBuy.settings().getIgnoreCase("database", "user", "loadall").asBoolean(true);
        topLimit = PixelBuy.settings().getIgnoreCase("database", "top", "limit").asLong(-1L);
        topTime = PixelBuy.settings().getIgnoreCase("database", "top", "time").asLong(6000L);

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

        if (topTime > 0) {
            if (topTask < 0) {
                topTask = Bukkit.getScheduler().runTaskTimerAsynchronously(PixelBuy.get(), this::calculateTop, 100L, topTime).getTaskId();
            }
        } else if (topTask > 0) {
            Bukkit.getScheduler().cancelTask(topTask);
            topTask = -1;
        }

        if (!registered) {
            registered = true;
            Bukkit.getPluginManager().registerEvents(this, PixelBuy.get());
            loadOnlineUsers();
            if (userLoadAll) {
                loadUsers();
            }
        } else {
            execute(() -> {
                loadOnlineUsers();
                if (userLoadAll) {
                    loadUsers();
                }
            });
        }

        messenger.onLoad();
    }

    public void onDisable() {
        if (client != null) {
            messenger.onDisable();
            Bukkit.getOnlinePlayers().forEach(player -> {
                final StoreUser user = getCached(player.getUniqueId());
                if (user != null) {
                    saveData(user);
                    unloadUser(user);
                }
            });
            client.saveUsers(cached.values());
            client.onClose();
        }
    }

    public void onReload() {
        onDisable();
        onLoad();
    }

    public boolean isUserLoadAll() {
        return userLoadAll;
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
    public StoreUser getCached(int index) {
        return index < sorted.size() ? cached.get(sorted.get(index)) : null;
    }

    public int getIndex(@NotNull UUID uniqueId) {
        return sorted.indexOf(uniqueId);
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
        StoreUser loaded = client.getUser(uniqueId, username);
        if (loaded == null) {
            loaded = new StoreUser(uniqueId, username, 0.0f);
        }
        loaded.setLoaded(true);
        client.getOrders(uniqueId, loaded::addOrder);
        cached.put(uniqueId, loaded);
        return cached.get(uniqueId);
    }

    @NotNull
    public StoreUser getDataAsync(@NotNull UUID uniqueId, @NotNull String username) {
        final StoreUser cachedUser = cached.get(uniqueId);
        if (cachedUser != null) {
            return cachedUser;
        }
        cached.put(uniqueId, new StoreUser(uniqueId, username, 0.0f));
        execute(() -> {
            StoreUser user = client.getUser(uniqueId, username);
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
            loadOrders(foundUser);
        });
        return cached.get(uniqueId);
    }

    @Nullable
    public StoreUser getDataOrNull(@NotNull UUID uniqueId, @NotNull String username) {
        final StoreUser user = cached.get(uniqueId);
        if (user != null) {
            return user;
        }
        final StoreUser loaded = client.getUser(uniqueId, username);
        if (loaded != null) {
            loaded.setLoaded(true);
            client.getOrders(uniqueId, loaded::addOrder);
            cached.put(uniqueId, loaded);
        }
        return cached.get(uniqueId);
    }

    public void loadOrders(@NotNull StoreUser user) {
        user.setLoaded(true);
        client.getOrders(user.getUniqueId(), user::mergeOrder);
    }

    public void loadUser(@NotNull Player player) {
        final StoreUser user = loadUser(player.getUniqueId(), player.getName());
        PixelBuy.get().getStore().getCheckout().onJoin(user);
    }

    public StoreUser loadUser(@NotNull UUID uniqueId, @NotNull String username) {
        final StoreUser cachedUser = cached.get(uniqueId);
        if (cachedUser != null) {
            if (!cachedUser.isLoaded()) {
                loadOrders(cachedUser);
            }
            return cachedUser;
        }
        StoreUser user = client.getUser(uniqueId, username);
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
        loadOrders(foundUser);
        return foundUser;
    }

    public void loadUsers() {
        client.getUsers(user -> cached.put(user.getUniqueId(), user));
    }

    public void loadOnlineUsers() {
        Bukkit.getOnlinePlayers().forEach(this::loadUser);
    }

    public void unloadUser(@NotNull StoreUser user) {
        if (userLoadAll) {
            user.setLoaded(false);
            user.getOrders().clear();
        } else {
            cached.remove(user.getUniqueId());
        }
    }

    public void saveData(@NotNull StoreUser user) {
        client.saveUser(user);
        messenger.update(user);
    }

    public void saveData(@NotNull StoreOrder order) {
        client.saveOrder(order);
        messenger.update(order);
    }

    @NotNull
    public CompletableFuture<StoreUser> saveDataAsync(@Nullable StoreUser user) {
        if (user == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            saveData(user);
            return user;
        }, this);
    }

    @NotNull
    public CompletableFuture<StoreOrder> saveDataAsync(@Nullable StoreOrder order) {
        if (order == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            saveData(order);
            return order;
        }, this);
    }

    public void sendProcess(@NotNull StoreUser user) {
        messenger.process(user, PixelBuy.get().getStore().getGroup());
    }

    public void updatedDonated(@NotNull StoreUser user) {
        messenger.update(user);
    }

    public void deleteData(@NotNull StoreOrder order) {
        client.deleteOrder(order.getProvider(), order.getId());
        messenger.delete(order);
    }

    public void deleteDataAsync(@NotNull StoreOrder order, @Nullable Runnable runnable) {
        execute(() -> {
            deleteData(order);
            if (runnable != null) {
                runnable.run();
            }
        });
    }

    public void calculateTop() {
        var stream = cached.entrySet().stream()
                .sorted(Comparator.<Map.Entry<UUID, StoreUser>>comparingDouble(entry -> entry.getValue().getDonated()).reversed());
        if (topLimit > 0) {
            stream = stream.limit(topLimit);
        }
        sorted = stream.map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (!Bukkit.isPrimaryThread()) {
            command.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), command);
        }
    }
}
