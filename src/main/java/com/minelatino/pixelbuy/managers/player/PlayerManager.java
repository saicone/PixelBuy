package com.minelatino.pixelbuy.managers.player;

import com.minelatino.pixelbuy.PixelBuy;

import com.minelatino.pixelbuy.managers.store.StoreItem;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerManager {

    private final PixelBuy pl = PixelBuy.get();

    private final Map<Player, PlayerData> players = new HashMap<>();

    public PlayerManager() {
        loadPlayers();
    }

    public void shut() {
        players.clear();
    }

    public void loadPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::loadPlayer);
    }

    public void loadPlayer(Player player) {
        PlayerData pData = pl.getDatabase().getData((pl.configBoolean("Database.UUID") ? player.getUniqueId().toString() : player.getName()));
        if (pData != null) {
            double donated = pData.getDonated();
            List<PlayerData.Order> orders = new ArrayList<>();
            for (PlayerData.Order order : pData.getOrders()) {
                if (order.hasPending()) {
                    Map<String, Byte> items = new HashMap<>();
                    for (Map.Entry<String, Byte> item : order.getItems().entrySet()) {
                        if (item.getValue() == 1) {
                            StoreItem sItem = pl.getStore().getItem(item.getKey());
                            sItem.getActions().forEach(action -> action.executeBuy(player.getName(), order.getId()));
                            items.put(item.getKey(), (byte) 2);
                            donated += Double.parseDouble(sItem.getPrice());
                        } else {
                            items.put(item.getKey(), item.getValue());
                        }
                    }
                    orders.add(new PlayerData.Order(order.getId(), items));
                } else {
                    orders.add(order);
                }
            }
            pData.setOrders(orders);
            pData.setDonated(donated);
            players.put(player, pData);
        }
    }

    public void unloadPlayer(Player player) {
        pl.getDatabase().saveData(players.get(player));
        players.remove(player);
    }

    public void processOrder(String player, PlayerData.Order order) {
        Player p = Utils.getPlayer(player);
        double donated = 0.00;
        Map<String, Byte> items = new HashMap<>();
        for (String item : order.getItems().keySet()) {
            StoreItem sItem = pl.getStore().getItem(item);
            if (sItem != null) {
                if (!sItem.isOnline() || p != null) {
                    sItem.getActions().forEach(action -> action.executeBuy(player, order.getId()));
                    items.put(item, (byte) 2);
                    donated += Double.parseDouble(sItem.getPrice());
                } else {
                    items.put(item, (byte) 1);
                }
            }
        }
        order.setItems(items);
        PlayerData pData = getPlayerData(player);
        if (pData == null) {
            pData = new PlayerData((pl.configBoolean("Database.UUID") ? (p == null ? Utils.getOfflineUUID(player) : p.getUniqueId().toString()) : player), donated, Collections.singletonList(order));
        } else {
            pData.addOrder(order);
        }
        saveDataChanges(p, pData);
    }

    public boolean refundOrder(String player, Integer orderID) {
        Player p = Utils.getPlayer(player);
        PlayerData pData = getPlayerData(player);
        if (pData != null) {
            boolean exists = false;
            List<PlayerData.Order> orders = new ArrayList<>();
            for (PlayerData.Order order : pData.getOrders()) {
                if (order.getId().equals(orderID)) {
                    Map<String, Byte> items = new HashMap<>();
                    for (String item : order.getItems().keySet()) {
                        pl.getStore().getItem(item).refund(player, orderID);
                        items.put(item, (byte) 3);
                    }
                    orders.add(new PlayerData.Order(order.getId(), items));
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

    public void saveDataChanges(Player player, PlayerData playerData) {
        if (player != null) {
            players.remove(player);
            players.put(player, playerData);
        } else {
            pl.getDatabase().saveData(playerData);
        }
    }

    public PlayerData getPlayerData(String player) {
        Player p = Utils.getPlayer(player);
        return (p != null ? players.getOrDefault(p, null) : pl.getDatabase().getData((pl.configBoolean("Database.UUID") ? Utils.getOfflineUUID(player) : player)));
    }
}
