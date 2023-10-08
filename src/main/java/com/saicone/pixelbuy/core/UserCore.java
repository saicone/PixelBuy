package com.saicone.pixelbuy.core;

import com.saicone.pixelbuy.PixelBuy;

import com.saicone.pixelbuy.api.object.StoreUser;
import com.saicone.pixelbuy.api.object.StoreItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class UserCore {

    private final PixelBuy pl = PixelBuy.get();

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

    public void loadPlayer(Player player) {
        StoreUser pData = pl.getDatabase().getData((pl.configBoolean("Database.UUID") ? player.getUniqueId().toString() : player.getName()));
        if (pData != null) {
            players.put(player.getUniqueId(), processData(player, pData));
        }
    }

    public void unloadPlayer(Player player) {
        pl.getDatabase().saveData(players.get(player.getUniqueId()));
        players.remove(player.getUniqueId());
    }

    public void processOrder(String player, StoreUser.Order order) {
        Player p = Bukkit.getPlayer(player);
        StoreUser pData = getPlayerData(player);
        if (pData != null) {
            if (isDuplicated(order.getId(), pData.getOrders())) return;
            pData.addOrder(order);
        } else {
            pData = new StoreUser((pl.configBoolean("Database.UUID") ? (p == null ? Bukkit.getOfflinePlayer(player).getUniqueId().toString() : p.getUniqueId().toString()) : player), 0.00, Collections.singletonList(order));
        }
        saveDataChanges(p, processData(p, pData));
    }

    public StoreUser processData(Player player, StoreUser data) {
        double donated = data.getDonated();
        List<StoreUser.Order> orders = data.getOrders(false);
        for (StoreUser.Order order : data.getOrders(true)) {
            if (!isDuplicated(order.getId(), orders)) {
                Map<String, Byte> items = order.getItems((byte) 2);
                for (Map.Entry<String, Byte> item : order.getItems((byte) 1).entrySet()) {
                    StoreItem sItem = pl.getStore().getItem(item.getKey());
                    if (sItem != null) {
                        if (!sItem.isOnline() || player != null) {
                            Bukkit.getScheduler().runTaskLater(pl, () -> sItem.buy(data.getPlayer(), order.getId()), pl.getFiles().getConfig().getInt("Order.Delay", 5) * 20);
                            items.put(item.getKey(), (byte) 2);
                            if (!sItem.getIdentifier().endsWith("-copy")) {
                                donated = donated + Double.parseDouble(sItem.getPrice());
                            }
                        } else {
                            items.put(item.getKey(), item.getValue());
                        }
                    }
                }
                orders.add(new StoreUser.Order(order.getId(), items));
            }
        }
        data.setOrders(orders);
        data.setDonated(donated);
        return data;
    }

    public boolean refundOrder(String player, Integer orderID) {
        Player p = Bukkit.getPlayer(player);
        StoreUser pData = getPlayerData(player);
        if (pData != null) {
            boolean exists = false;
            List<StoreUser.Order> orders = new ArrayList<>();
            for (StoreUser.Order order : pData.getOrders()) {
                if (order.getId().equals(orderID)) {
                    Map<String, Byte> items = new HashMap<>();
                    for (Map.Entry<String, Byte> item : order.getItems().entrySet()) {
                        if (item.getValue() == 2) {
                            pl.getStore().getItem(item.getKey()).refund(player, orderID);
                        }
                        items.put(item.getKey(), (byte) 3);
                    }
                    orders.add(new StoreUser.Order(order.getId(), items));
                    exists = true;
                } else {
                    orders.add(order);
                }
            }
            pData.setOrders(orders);
            if (exists) saveDataChanges(p, pData);
            return exists;
        }
        return false;
    }

    public void saveDataChanges(Player player, StoreUser storeUser) {
        if (player != null) {
            players.remove(player.getUniqueId());
            players.put(player.getUniqueId(), storeUser);
        } else {
            pl.getDatabase().saveData(storeUser);
        }
    }

    public void saveDataChanges(String name, StoreUser data) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            players.put(player.getUniqueId(), data);
        }
        pl.getDatabase().saveData(data);
    }

    public StoreUser getPlayerData(String player) {
        Player p = Bukkit.getPlayer(player);
        return (p != null ? players.getOrDefault(p.getUniqueId(), null) : pl.getDatabase().getData((pl.configBoolean("Database.UUID") ? Bukkit.getOfflinePlayer(player).getUniqueId().toString() : player)));
    }

    private boolean isDuplicated(Integer id, List<StoreUser.Order> list) {
        for (StoreUser.Order order : list) {
            if (order.getId().equals(id)) return true;
        }
        return false;
    }
}
