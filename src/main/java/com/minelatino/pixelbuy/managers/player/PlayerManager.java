package com.minelatino.pixelbuy.managers.player;

import com.minelatino.pixelbuy.PixelBuy;

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
        if (pData != null) players.put(player, pData);
    }

    public void unloadPlayer(Player player) {
        pl.getDatabase().saveData(getPlayerData(player));
        players.remove(player);
    }

    public void addOrder(String player, PlayerData.Order order) {
        Player p = Utils.getPlayer(player);
        if (p != null) {
            PlayerData pData = getPlayerData(p);
            if (pData != null) {
                pData.addOrder(order);
            } else {
                pData = new PlayerData(player, 0, Collections.singletonList(order));
                players.put(p, pData);
            }
            processPlayer(p);
        } else {
            PlayerData pData = getPlayerData(player);
            if (pData != null) {
                pData.addOrder(order);
            } else {
                pData = new PlayerData(player, 0, Collections.singletonList(order));
            }
            pl.getDatabase().saveData(pData);
        }
    }

    public PlayerData getPlayerData(String player) {
        return pl.getDatabase().getData(player);
    }

    public PlayerData getPlayerData(Player player) {
        return players.getOrDefault(player, null);
    }

    public void processPlayer(Player player) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.getPendingOrders().forEach(order -> {

                data.changeOrderState(order, 2);
            });
        }
    }
}
