package com.minelatino.pixelbuy.module.locale;

import com.minelatino.pixelbuy.PixelBuyBukkit;
import com.minelatino.pixelbuy.module.config.Settings;
import com.minelatino.pixelbuy.util.ReflectUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class BukkitLocale extends PixelLocale {

    private final String version;
    private final int verNumber;
    private boolean titleCompatible = false;
    private boolean usingSpigot = false;
    private boolean useRGB = false;

    public BukkitLocale(Settings settings) {
        super(settings);
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        verNumber = Integer.parseInt(version.split("_")[1]);
        if (verNumber >= 9) {
            try {
                Class.forName("org.spigotmc.SpigotConfig");
                usingSpigot = true;
            } catch (ClassNotFoundException ignored) { }
            if (verNumber >= 12) {
                titleCompatible = true;
                if (verNumber >= 16) {
                    useRGB = true;
                }
            }
        }
        load();
        reload();
    }

    @Override
    void load() {
        if (titleCompatible && usingSpigot) return;
        try {
            ReflectUtils.addClass("CraftPlayer", Class.forName("net.minecraft.server." + version + ".entity.CraftPlayer"));
            ReflectUtils.addClass("IChatBaseComponent", Class.forName("net.minecraft.server." + version + ".IChatBaseComponent"));
            ReflectUtils.addClass("ChatSerializer", ReflectUtils.getClass("IChatBaseComponent").getDeclaredClasses()[0]);
            ReflectUtils.addClass("Packet", Class.forName("net.minecraft.server." + version + ".Packet"));

            ReflectUtils.addMethod("ChatSerializer.a", ReflectUtils.getClass("ChatSerializer").getMethod("a", String.class));
            ReflectUtils.addMethod("CraftPlayer.getHandle", ReflectUtils.getClass("CraftPlayer").getMethod("getHandle"));
            ReflectUtils.addMethod("PlayerConnection.sendPacket", Class.forName("net.minecraft.server." + version + ".PlayerConnection").getMethod("sendPacket", ReflectUtils.getClass("Packet")));

            ReflectUtils.addField("playerConnection", Class.forName("net.minecraft.server." + version + ".EntityPlayer").getField("playerConnection"));

            if (!titleCompatible) {
                ReflectUtils.addClass("PacketPlayOutTitle", Class.forName("net.minecraft.server." + version + ".PacketPlayOutTitle"));

                ReflectUtils.addField("TIMES", ReflectUtils.getClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES"));
                ReflectUtils.addField("TITLE", ReflectUtils.getClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE"));
                ReflectUtils.addField("SUBTITLE", ReflectUtils.getClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE"));

                ReflectUtils.addConstructor("PacketPlayOutTitle", ReflectUtils.getClass("PacketPlayOutTitle").getDeclaredConstructor(ReflectUtils.getClass("PacketPlayOutTitle").getDeclaredClasses()[0], ReflectUtils.getClass("IChatBaseComponent"), int.class, int.class, int.class));
            }

            if (!usingSpigot) {
                ReflectUtils.addClass("PacketPlayOutChat", Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat"));
                ReflectUtils.addClass("ChatMessageType", Class.forName("net.minecraft.server." + version + ".ChatMessageType"));

                if (verNumber >= 12) {
                    ReflectUtils.addMethod("ChatMessageType.a", ReflectUtils.getClass("ChatMessageType").getMethod("a", byte.class));
                }

                ReflectUtils.addConstructor("PacketPlayOutChat", ReflectUtils.getClass("PacketPlayOutChat").getDeclaredConstructor(ReflectUtils.getClass("IChatBaseComponent"), (verNumber >= 12 ? ReflectUtils.getClass("ChatMessageType") : byte.class)));
            }
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendToConsole(String path, String... args) {
        sendTo(Bukkit.getConsoleSender(), path, args);
    }

    @Override
    public void sendTo(Object user, String path, String... args) {
        text((CommandSender) user, replaceArgs(file.getStringList(path), args));
    }

    @Override
    public void sendMessage(Object user, String text, String... args) {
        ((CommandSender) user).sendMessage(replaceArgs(text, args));
    }

    @Override
    public void broadcast(String text, String... args) {
        String msg = replaceArgs(text, args);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(msg));
    }

    @Override
    public void broadcastPath(String path, String... args) {
        List<String> msg = replaceArgs(file.getStringList(path), args);
        Bukkit.getOnlinePlayers().forEach(player -> text(player, msg));
    }

    private void text(CommandSender sender, List<String> list) {
        list.forEach(sender::sendMessage);
    }

    @Override
    public void sendTitle(Object user, String path, String... args) {
        if (user instanceof Player) {
            title((Player) user, replaceArgs(file.getString(path + ".title"), args), replaceArgs(file.getString(path + ".subtitle"), args), file.getInt(path + ".fadeIn"), file.getInt(path + ".stay"), file.getInt(path + ".fadeOut"));
        }
    }

    @Override
    public void sendTitle(Object user, String title, String subtitle, int fadeIn, int stay, int fadeOut, String... args) {
        if (user instanceof Player) {
            title((Player) user, replaceArgs(title, args), replaceArgs(subtitle, args), fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void broadcastTitle(String path, String... args) {
        String title = replaceArgs(file.getString(path + ".title"), args);
        String subtitle = replaceArgs(file.getString(path + ".subtitle"), args);
        int fadeIn = file.getInt(path + ".fadeIn");
        int stay = file.getInt(path + ".stay");
        int fadeOut = file.getInt(path + ".fadeOut");
        Bukkit.getOnlinePlayers().forEach(player -> title(player, title, subtitle, fadeIn, stay, fadeOut));
    }

    @Override
    public void broadcastTile(String title, String subtitle, int fadeIn, int stay, int fadeOut, String... args) {
        String title0 = replaceArgs(title, args);
        String subtitle0 = replaceArgs(subtitle, args);
        Bukkit.getOnlinePlayers().forEach(player -> title(player, title0, subtitle0, fadeIn, stay, fadeOut));
    }

    private void title(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (titleCompatible) {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        } else {
            packetTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    // TODO: Remove this in future by dropping <1.12 versions compatibility
    private void packetTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if ((title == null || title.isEmpty()) && (subtitle == null || subtitle.isEmpty())) return;
        try {
            Object playerConnection = ReflectUtils.getField("playerConnection").get(ReflectUtils.getMethod("CraftPlayer.getHandle").invoke(ReflectUtils.getClass("CraftPlayer").cast(player)));

            Object chatTitle = ReflectUtils.getMethod("ChatSerializer.a").invoke(ReflectUtils.getClass("ChatSerializer"), "{\"text\" : \"" + (title != null ? title : "") + "\"}");
            Object packet1 = ReflectUtils.getConstructor("PacketPlayOutTitle").newInstance(ReflectUtils.getField("TIMES").get(null), chatTitle, fadeIn, stay, fadeOut);
            ReflectUtils.getMethod("PlayerConnection.sendPacket").invoke(playerConnection, ReflectUtils.getClass("Packet").cast(packet1));

            if (title != null && !title.isEmpty()) {
                Object packet2 = ReflectUtils.getConstructor("PacketPlayOutTitle").newInstance(ReflectUtils.getField("TITLE").get(null), chatTitle, fadeIn, stay, fadeOut);
                ReflectUtils.getMethod("PlayerConnection.sendPacket").invoke(playerConnection, ReflectUtils.getClass("Packet").cast(packet2));
            }

            if (subtitle != null && !subtitle.isEmpty()) {
                Object chatSubTitle = ReflectUtils.getMethod("ChatSerializer.a").invoke(ReflectUtils.getClass("ChatSerializer"), "{\"text\" : \"" + subtitle + "\"}");
                Object packet3 = ReflectUtils.getConstructor("PacketPlayOutTitle").newInstance(ReflectUtils.getField("SUBTITLE").get(null), chatSubTitle, fadeIn, stay, fadeOut);
                ReflectUtils.getMethod("PlayerConnection.sendPacket").invoke(playerConnection, ReflectUtils.getClass("Packet").cast(packet3));
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendActionbar(Object user, String path, String... args) {
        if (user instanceof Player) {
            actionbar((Player) user, replaceArgs(file.getString(path + ".text"), args), file.getInt(path + ".pulses"));
        }
    }

    @Override
    public void sendActionbar(Object user, String text, int pulses, String... args) {
        if (user instanceof Player) {
            actionbar((Player) user, replaceArgs(text, args), pulses);
        }
    }

    @Override
    public void broadcastActionbar(String path, String... args) {
        String text = replaceArgs(file.getString(path + ".text"), args);
        int pulses = file.getInt(path + ".pulses");
        Bukkit.getOnlinePlayers().forEach(player -> actionbar(player, text, pulses));
    }

    @Override
    public void broadcastActionbar(String text, int pulses, String... args) {
        String text0 = replaceArgs(text, args);
        Bukkit.getOnlinePlayers().forEach(player -> actionbar(player, text0, pulses));
    }

    private void actionbar(Player player, String text, int pulses) {
        if (text == null || text.isEmpty()) return;
        if (usingSpigot) {
            if (pulses == 1) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
            } else {
                BaseComponent[] msg = TextComponent.fromLegacyText(text);
                new BukkitRunnable() {
                    int times = 1;
                    @Override
                    public void run() {
                        if (times > pulses) {
                            cancel();
                        } else {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg);
                            times++;
                        }
                    }
                }.runTaskTimer(PixelBuyBukkit.get(), 1, 10);
            }
        } else {
            packetActionbar(player, text, pulses);
        }
    }

    private void packetActionbar(Player player, String text, int pulses) {
        try {
            Object chatActionbar = ReflectUtils.getMethod("ChatSerializer.a").invoke(ReflectUtils.getClass("ChatSerializer"), "{\"text\" : \"" + text + "\"}");

            Object packet;
            if (verNumber >= 16) {
                packet = ReflectUtils.getConstructor("PacketPlayOutChat").newInstance(ReflectUtils.getClass("IChatBaseComponent").cast(chatActionbar), ReflectUtils.getMethod("ChatMessageType.a").invoke(ReflectUtils.getClass("ChatMessageType"), (byte) 2));
            } else if (verNumber >= 12) {
                packet = ReflectUtils.getConstructor("PacketPlayOutChat").newInstance(chatActionbar, ReflectUtils.getMethod("ChatMessageType.a").invoke(ReflectUtils.getClass("ChatMessageType"), (byte) 2));
            } else {
                packet = ReflectUtils.getConstructor("PacketPlayOutChat").newInstance(chatActionbar, (byte) 2);
            }

            Object playerConnection = ReflectUtils.getField("playerConnection").get(ReflectUtils.getMethod("CraftPlayer.getHandle").invoke(ReflectUtils.getClass("CraftPlayer").cast(player)));
            if (pulses == 1) {
                ReflectUtils.getMethod("PlayerConnection.sendPacket").invoke(playerConnection, ReflectUtils.getClass("Packet").cast(packet));
            } else {
                new BukkitRunnable() {
                    int times = 1;
                    @Override
                    public void run() {
                        if (times > pulses) {
                            cancel();
                        } else {
                            try {
                                ReflectUtils.getMethod("PlayerConnection.sendPacket").invoke(playerConnection, ReflectUtils.getClass("Packet").cast(packet));
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            times++;
                        }
                    }
                }.runTaskTimer(PixelBuyBukkit.get(), 1, 10);
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String color(String s) {
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

    private String rgb(String color) {
        try {
            Integer.parseInt(color, 16);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid HEX color: #" + color);
        }

        StringBuilder hex = new StringBuilder("§x");
        for (char c : color.toCharArray()) {
            hex.append("§").append(c);
        }

        return hex.toString();
    }
}
