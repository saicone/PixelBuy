package com.minelatino.pixelbuy.module.locale.user;

import com.minelatino.pixelbuy.PixelBuy;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeUser extends UserType<CommandSender> {

    @Override
    boolean isConsole(CommandSender user) {
        return !(user instanceof ProxiedPlayer);
    }

    @Override
    public String getName(CommandSender user) {
        if (user instanceof ProxiedPlayer) {
            return user.getName();
        } else {
            return null;
        }
    }

    @Override
    public UUID getUniqueId(CommandSender user) {
        if (user instanceof ProxiedPlayer) {
            return ((ProxiedPlayer) user).getUniqueId();
        } else {
            return CONSOLE_UUID;
        }
    }

    @Override
    public boolean hasPermission(CommandSender user, String permission) {
        return user.hasPermission(permission);
    }

    @Override
    public void sendMessage(CommandSender user, String msg, String... args) {
        user.sendMessage(TextComponent.fromLegacyText(PixelBuy.LOCALE.replaceArgs(msg, args)));
    }
}
