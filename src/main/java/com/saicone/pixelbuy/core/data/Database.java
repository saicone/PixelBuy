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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Database implements Listener {

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
        loadUser(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        unloadUser(event.getPlayer());
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
            loadOnlineUsers(true);
            registered = true;
            Bukkit.getPluginManager().registerEvents(this, PixelBuy.get());
            if (userLoadAll) {
                loadUsers(true);
            }
        } else if (userLoadAll) {
            loadOnlineUsers(false);
            loadUsers(false);
        }

        messenger.onLoad();
    }

    public void onDisable() {
        if (client != null) {
            messenger.onDisable();
            Bukkit.getOnlinePlayers().forEach(this::unloadUser);
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

    public void loadUser(@NotNull Player player) {
        loadUser(player, user -> PixelBuy.get().getStore().getCheckout().onJoin(user));
    }

    public void loadUser(@NotNull Player player, @NotNull Consumer<StoreUser> consumer) {
        loadUser(false, player.getUniqueId(), player.getName(), consumer);
    }

    public void loadUser(boolean sync, @NotNull UUID uniqueId, @NotNull String username, @NotNull Consumer<StoreUser> consumer) {
        final StoreUser cachedUser = cached.get(uniqueId);
        if (cachedUser != null) {
            if (!cachedUser.isLoaded()) {
                if (!sync) {
                    Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> {
                        loadOrders(true, cachedUser);
                        consumer.accept(cachedUser);
                    });
                    return;
                }
                loadOrders(true, cachedUser);
            }
            consumer.accept(cachedUser);
            return;
        }
        if (!sync) {
            // Add temp value
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

    public void loadOnlineUsers(boolean sync) {
        if (sync) {
            Bukkit.getOnlinePlayers().forEach(this::loadUser);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> Bukkit.getOnlinePlayers().forEach(this::loadUser));
        }
    }

    public void unloadUser(@NotNull Player player) {
        saveDataAsync(getCached(player.getUniqueId()), this::unloadUser);
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
        client.saveOrders(Collections.singleton(order));
        messenger.update(order);
    }

    public void saveDataAsync(@Nullable StoreUser user, @Nullable Consumer<StoreUser> consumer) {
        if (user != null) {
            Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> {
                saveData(user);
                if (consumer != null) {
                    consumer.accept(user);
                }
            });
        }
    }

    public void saveDataAsync(@Nullable StoreOrder order, @Nullable Consumer<StoreOrder> consumer) {
        if (order != null) {
            Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> {
                saveData(order);
                if (consumer != null) {
                    consumer.accept(order);
                }
            });
        }
    }

    public void sendProcess(@NotNull StoreUser user) {
        messenger.process(user);
    }

    public void deleteData(@NotNull StoreOrder order) {
        client.deleteOrder(order.getProvider(), order.getId());
        messenger.delete(order);
    }

    public void deleteDataAsync(@NotNull StoreOrder order, @Nullable Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> {
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
}
