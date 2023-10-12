package com.saicone.pixelbuy.core;

import com.saicone.pixelbuy.PixelBuy;

import com.saicone.pixelbuy.api.object.StoreUser;
import com.saicone.pixelbuy.api.object.StoreItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UserCore {

    private final PixelBuy plugin = PixelBuy.get();

    private final Map<UUID, StoreUser> players = new HashMap<>();

    public UserCore() {
        loadPlayers();
    }

    public void shut() {
        Bukkit.getOnlinePlayers().forEach(this::unloadPlayer);
        players.clear();
    }

    public void loadPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::loadPlayer);
    }

    public void loadPlayer(@NotNull Player player) {
        final StoreUser user = plugin.getDatabase().getData((PixelBuy.settings().getBoolean("Database.UUID") ? player.getUniqueId().toString() : player.getName()));
        if (user != null) {
            players.put(player.getUniqueId(), processData(player, user));
        }
    }

    public void unloadPlayer(@NotNull Player player) {
        plugin.getDatabase().saveData(players.get(player.getUniqueId()));
        players.remove(player.getUniqueId());
    }

    @SuppressWarnings("deprecation")
    public void processOrder(@NotNull String player, @NotNull StoreUser.Order order) {
        final Player onlinePlayer = Bukkit.getPlayer(player);
        StoreUser user = getPlayerData(player);
        if (user != null) {
            if (isDuplicated(order.getId(), user.getOrders())) {
                return;
            }
            user.addOrder(order);
        } else {
            user = new StoreUser((PixelBuy.settings().getBoolean("Database.UUID") ? (onlinePlayer == null ? Bukkit.getOfflinePlayer(player).getUniqueId().toString() : onlinePlayer.getUniqueId().toString()) : player), 0.00, Collections.singletonList(order));
        }
        saveDataChanges(onlinePlayer, processData(onlinePlayer, user));
    }

    @NotNull
    public StoreUser processData(@Nullable Player player, @NotNull StoreUser user) {
        double donated = user.getDonated();
        final List<StoreUser.Order> orders = user.getOrders(false);
        for (StoreUser.Order order : user.getOrders(true)) {
            if (!isDuplicated(order.getId(), orders)) {
                final Map<String, Byte> items = order.getItems((byte) 2);
                for (Map.Entry<String, Byte> item : order.getItems((byte) 1).entrySet()) {
                    final StoreItem storeItem = plugin.getStore().getItem(item.getKey());
                    if (storeItem != null) {
                        if (!storeItem.isOnline() || player != null) {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> storeItem.buy(user.getPlayer(), order.getId()), PixelBuy.settings().getLong("Order.Delay", 5L) * 20);
                            items.put(item.getKey(), (byte) 2);
                            if (!storeItem.getIdentifier().endsWith("-copy")) {
                                donated = donated + Double.parseDouble(storeItem.getPrice());
                            }
                        } else {
                            items.put(item.getKey(), item.getValue());
                        }
                    }
                }
                orders.add(new StoreUser.Order(order.getId(), items));
            }
        }
        user.setOrders(orders);
        user.setDonated(donated);
        return user;
    }

    public boolean refundOrder(@NotNull String player, int orderID) {
        final Player onlinePlayer = Bukkit.getPlayer(player);
        final StoreUser user = getPlayerData(player);
        if (user != null) {
            boolean exists = false;
            final List<StoreUser.Order> orders = new ArrayList<>();
            for (StoreUser.Order order : user.getOrders()) {
                if (order.getId().equals(orderID)) {
                    final Map<String, Byte> items = new HashMap<>();
                    for (Map.Entry<String, Byte> item : order.getItems().entrySet()) {
                        if (item.getValue() == 2) {
                            plugin.getStore().getItem(item.getKey()).refund(player, orderID);
                        }
                        items.put(item.getKey(), (byte) 3);
                    }
                    orders.add(new StoreUser.Order(order.getId(), items));
                    exists = true;
                } else {
                    orders.add(order);
                }
            }
            user.setOrders(orders);
            if (exists) {
                saveDataChanges(onlinePlayer, user);
            }
            return exists;
        }
        return false;
    }

    public void saveDataChanges(@Nullable Player player, @NotNull StoreUser user) {
        if (player != null) {
            players.remove(player.getUniqueId());
            players.put(player.getUniqueId(), user);
        } else {
            plugin.getDatabase().saveData(user);
        }
    }

    public void saveDataChanges(@NotNull String name, @NotNull StoreUser user) {
        final Player player = Bukkit.getPlayer(name);
        if (player != null) {
            players.put(player.getUniqueId(), user);
        }
        plugin.getDatabase().saveData(user);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public StoreUser getPlayerData(@NotNull String player) {
        final Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null) {
            return players.getOrDefault(onlinePlayer.getUniqueId(), null);
        } else {
            return plugin.getDatabase().getData(PixelBuy.settings().getBoolean("Database.UUID") ? Bukkit.getOfflinePlayer(player).getUniqueId().toString() : player);
        }
    }

    private boolean isDuplicated(int id, @NotNull List<StoreUser.Order> list) {
        for (StoreUser.Order order : list) {
            if (order.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}
