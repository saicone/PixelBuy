package com.saicone.pixelbuy.core.command;

import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.module.command.BukkitCommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class PixelCommand extends BukkitCommandNode {

    public PixelCommand(@NotNull String id) {
        super(id);
    }

    public PixelCommand(@NotNull String id, @NotNull BukkitCommandNode... subCommands) {
        super(id, subCommands);
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission("pixelbuy.*") || super.hasPermission(sender);
    }

    @Override
    public void sendPermissionMessage(@NotNull CommandSender sender) {
        Lang.COMMAND_NO_PERM.sendTo(sender, getPermission());
    }

    @Override
    public void sendSubUsage(@NotNull CommandSender sender) {

    }
}
