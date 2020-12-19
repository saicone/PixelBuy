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
        Bukkit.getOnlinePlayers().forEach(this::unloadPlayer);
        players.clear();
    }

    public void loadPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::loadPlayer);
    }

    public void loadPlayer(Player player) {
        PlayerData pData = pl.getDatabase().getData((pl.configBoolean("Database.UUID") ? player.getUniqueId().toString() : player.getName()));
        if (pData != null) {
            players.put(player, processData(player, pData));
        }
    }

    public void unloadPlayer(Player player) {
        pl.getDatabase().saveData(players.get(player));
        players.remove(player);
    }

    public void processOrder(String player, PlayerData.Order order) {
        Player p = Utils.getPlayer(player);
        PlayerData pData = getPlayerData(player);
        if (pData != null) {
            if (isDuplicated(order.getId(), pData.getOrders())) return;
            pData.addOrder(order);
        } else {
            pData = new PlayerData((pl.configBoolean("Database.UUID") ? (p == null ? Utils.getOfflineUUID(player) : p.getUniqueId().toString()) : player), 0.00, Collections.singletonList(order));
        }
        saveDataChanges(p, processData(p, pData));
    }

    public PlayerData processData(Player player, PlayerData data) {
        double donated = data.getDonated();
        List<PlayerData.Order> orders = data.getOrders(false);
        for (PlayerData.Order order : data.getOrders(true)) {
            if (!isDuplicated(order.getId(), orders)) {
                Map<String, Byte> items = order.getItems((byte) 2);
                for (Map.Entry<String, Byte> item : order.getItems((byte) 1).entrySet()) {
                    StoreItem sItem = pl.getStore().getItem(item.getKey());
                    if (sItem != null) {
                        if (!sItem.isOnline() || player != null) {
                            Bukkit.getScheduler().runTaskLater(pl, () -> sItem.buy(data.getPlayer(), order.getId()), pl.getFiles().getConfig().getInt("Order.Delay", 5) * 20);
                            items.put(item.getKey(), (byte) 2);
                            donated = donated + Double.parseDouble(sItem.getPrice());
                        } else {
                            items.put(item.getKey(), item.getValue());
                        }
                    }
                }
                orders.add(new PlayerData.Order(order.getId(), items));
            }
        }
        data.setOrders(orders);
        data.setDonated(donated);
        return data;
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
                    for (Map.Entry<String, Byte> item : order.getItems().entrySet()) {
                        if (item.getValue() == 2) {
                            pl.getStore().getItem(item.getKey()).refund(player, orderID);
                        }
                        items.put(item.getKey(), (byte) 3);
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

    private boolean isDuplicated(Integer id, List<PlayerData.Order> list) {
        for (PlayerData.Order order : list) {
            if (order.getId().equals(id)) return true;
        }
        return false;
    }
}
