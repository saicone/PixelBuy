package com.minelatino.pixelbuy.managers.player;

import com.minelatino.pixelbuy.PixelBuy;

import com.minelatino.pixelbuy.managers.store.StoreItem;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private final PixelBuy pl = PixelBuy.get();

    private final Map<Player, PlayerData> players = new HashMap<>();

    public PlayerManager() {
        loadPlayers();
    }

    public void loadPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::loadPlayer);
    }

    public void loadPlayer(Player player) {
        PlayerData pData = pl.getDatabase().getData((pl.configBoolean("Database.UUID") ? player.getUniqueId().toString() : player.getName()));
        if (pData != null) {
            int donated = pData.getDonated();
            for (PlayerData.Order order : pData.getPendingOrders()) {
                for (Map.Entry<String, Byte> item : order.getItems().entrySet()) {
                    if (item.getValue() == 1) {
                        StoreItem sItem = pl.getStore().getItem(item.getKey());
                        sItem.getActions().forEach(action -> action.executeBuy(player, order.getId()));
                        order.setItemState(item.getKey(), (byte) 2);
                        donated+= Integer.parseInt(sItem.getPrice());
                    }
                }
            }
            pData.setDonated(donated);
            players.put(player, pData);
        }
    }

    public void unloadPlayer(Player player) {
        pl.getDatabase().saveData(players.get(player));
        players.remove(player);
    }

    @SuppressWarnings("deprecation")
    public void processOrder(String player, PlayerData.Order order) {
        Player p = Utils.getPlayer(player);
        int donated = 0;
        for (String item : order.getItems().keySet()) {
            StoreItem sItem = pl.getStore().getItem(item);
            if (sItem == null) {
                order.removeItem(item);
            } else if (!sItem.isOnline() || p != null) {
                sItem.getActions().forEach(action -> action.executeBuy(player, order.getId()));
                order.setItemState(item, (byte) 2);
                donated+= Integer.parseInt(sItem.getPrice());
            }
        }
        PlayerData pData = getPlayerData(player);
        if (pData == null) {
            pData = new PlayerData((pl.configBoolean("Database.UUID") ? (p == null ? Bukkit.getOfflinePlayer(player).getUniqueId().toString() : p.getUniqueId().toString()) : player), donated, Collections.singletonList(order));
        } else {
            pData.addOrder(order);
        }
        saveDataChanges(p, pData);
    }

    @SuppressWarnings("deprecation")
    public boolean refundOrder(String player, Integer orderID) {
        Player p = Utils.getPlayer(player);
        PlayerData pData = getPlayerData(player);
        if (pData != null) {
            boolean exists = false;
            for (PlayerData.Order order : pData.getOrders()) {
                if (order.getId().equals(orderID)) {
                    for (String item : order.getItems().keySet()) {
                        pl.getStore().getItem(item).refund((p == null ? (Player) Bukkit.getOfflinePlayer(player) : p), orderID);
                        order.setItemState(item, (byte) 3);
                    }
                    exists = true;
                }
            }
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
        if (p != null) return players.getOrDefault(p, null);
        return pl.getDatabase().getData(player);
    }
}
