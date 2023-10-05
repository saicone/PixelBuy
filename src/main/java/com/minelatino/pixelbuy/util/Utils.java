package com.minelatino.pixelbuy.util;

import com.minelatino.pixelbuy.PixelBuy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static final int verNumber;
    private static final boolean useRGB;

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        int versionNumber = Integer.parseInt(version.split("_")[1]);
        verNumber = versionNumber;
        useRGB = versionNumber >= 16;
    }

    public static List<String> color(List<String> list) {
        List<String> finalList = new ArrayList<>();
        for (String s : list) {
            finalList.add(color(s));
        }
        return finalList;
    }

    public static String color(String s) {
        if (useRGB && s.contains("&#")) {
            StringBuilder builder = new StringBuilder();
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (i + 7 < chars.length && chars[i] == '&' && chars[i + 1] == '#') {
                    StringBuilder color = new StringBuilder();
                    for (int c = i + 2; c < chars.length && c < 7; c++) {
                        color.append(chars[c]);
                    }
                    if (color.length() == 6) {
                        builder.append(rgb(color.toString()));
                        i += 8;
                    }
                } else {
                    builder.append(chars[i]);
                }
            }
            return ChatColor.translateAlternateColorCodes('&', builder.toString());
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private static String rgb(String color) {
        try {
            Integer.parseInt(color, 16);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return "<Invalid HEX>";
        }

        StringBuilder hex = new StringBuilder("§x");
        for (char c : color.toCharArray()) {
            hex.append("§").append(c);
        }

        return hex.toString();
    }

    public static List<String> replace(List<String> list, String from, String to) {
        List<String> finalList = new ArrayList<>();
        for (String s : list) {
            finalList.add(s.replace(from, to));
        }
        return finalList;
    }

    public static void info(String s) {
        PixelBuy.get().getLogger().info(color(s));
    }

    public static Player getPlayer(String s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(s)) return p;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static String getOfflineUUID(String s) {
        return Bukkit.getOfflinePlayer(s).getUniqueId().toString();
    }

    public static boolean isOnline(String player) {
        return getPlayer(player) != null;
    }
}
