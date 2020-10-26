package com.minelatino.pixelbuy.managers.player;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private final Map<Player, PlayerData> players = new HashMap<>();

    public PlayerManager() {
        reload(Bukkit.getConsoleSender());
    }

    public void reload(CommandSender sender) {
        players.clear();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            loadPlayer(p);
        }
    }

    public static void loadPlayer(Player player) {

    }

    public static void unloadPlayer(Player player) {

    }
}
