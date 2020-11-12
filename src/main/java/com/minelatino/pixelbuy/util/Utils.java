package com.minelatino.pixelbuy.util;

import com.minelatino.pixelbuy.PixelBuy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void info(String s) {
        PixelBuy.get().getLogger().info(color(s));
    }

    public static Player getPlayer(String s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().equals(s.toLowerCase())) return p;
        }
        return null;
    }
}
