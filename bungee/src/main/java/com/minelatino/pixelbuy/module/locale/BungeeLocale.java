package com.minelatino.pixelbuy.module.locale;

import com.minelatino.pixelbuy.PixelBuyBungee;
import com.minelatino.pixelbuy.module.config.Settings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class BungeeLocale extends PixelLocale {

    private final PixelBuyBungee pl;

    public BungeeLocale(PixelBuyBungee pl, Settings settings) {
        super(settings);
        this.pl = pl;
        reload();
    }

    @Override
    public void sendToConsole(String path, String... args) {
        sendTo(pl.getProxy().getConsole(), path, args);
    }

    @Override
    public void sendTo(Object user, String path, String... args) {
        if (user instanceof CommandSender) {
            text((CommandSender) user, translate(file.getStringList(path), args));
        }
    }

    @Override
    public void sendMessage(Object user, String text, String... args) {
        if (user instanceof CommandSender) {
            ((CommandSender) user).sendMessage(translate(text, args));
        }
    }

    @Override
    public void broadcast(String text, String... args) {
        BaseComponent[] msg = translate(text, args);
        pl.getProxy().getPlayers().forEach(player -> player.sendMessage(msg));
    }

    @Override
    public void broadcastPath(String path, String... args) {
        List<BaseComponent[]> msg = translate(file.getStringList(path), args);
        pl.getProxy().getPlayers().forEach(player -> text(player, msg));
    }

    private void text(CommandSender sender, List<BaseComponent[]> list) {
        list.forEach(sender::sendMessage);
    }

    @Override
    public void sendTitle(Object user, String path, String... args) {
        if (user instanceof ProxiedPlayer) {
            title((ProxiedPlayer) user, translate(file.getString(path + ".title"), args), translate(file.getString(path + ".subtitle"), args), file.getInt(path + ".fadeIn"), file.getInt(path + ".stay"), file.getInt(path + ".fadeOut"));
        }
    }

    @Override
    public void sendTitle(Object user, String title, String subtitle, int fadeIn, int stay, int fadeOut, String... args) {
        if (user instanceof ProxiedPlayer) {
            title((ProxiedPlayer) user, translate(title, args), translate(subtitle, args), fadeIn, stay, fadeOut);
        }
    }

    private void title(ProxiedPlayer player, BaseComponent[] title, BaseComponent[] subtitle, int fadeIn, int stay, int fadeOut) {
        pl.getProxy().createTitle().title(title).subTitle(subtitle).fadeIn(fadeIn).stay(stay).fadeOut(fadeOut).send(player);
    }

    @Override
    public void sendActionbar(Object user, String path, String... args) {
        if (user instanceof ProxiedPlayer) {
            actionbar((ProxiedPlayer) user, translate(file.getString(path + ".text"), args), file.getInt(path + ".pulses"));
        }
    }

    @Override
    public void sendActionbar(Object user, String text, int pulses, String... args) {
        if (user instanceof ProxiedPlayer) {
            actionbar((ProxiedPlayer) user, translate(text, args), pulses);
        }
    }

    private void actionbar(ProxiedPlayer player, BaseComponent[] text, int pulses) {
        player.sendMessage(ChatMessageType.ACTION_BAR, text);
    }

    @Override
    public String color(String s) {
        if (s.contains("&#")) {
            StringBuilder builder = new StringBuilder();
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (i + 7 < chars.length && chars[i] == '&' && chars[i + 1] == '#') {
                    StringBuilder color = new StringBuilder();
                    for (int c = i + 2; c < chars.length && c < 7; c++) {
                        color.append(chars[c]);
                    }
                    if (color.length() == 6) {
                        builder.append(ChatColor.of(color.toString()));
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

    private BaseComponent[] translate(String s, String... args) {
        return TextComponent.fromLegacyText(replaceArgs(s, args));
    }

    private List<BaseComponent[]> translate(List<String> list, String... args) {
        List<BaseComponent[]> l = new ArrayList<>();
        list.forEach(s -> l.add(TextComponent.fromLegacyText(replaceArgs(s, args))));
        return l;
    }
}
