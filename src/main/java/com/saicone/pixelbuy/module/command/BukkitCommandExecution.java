package com.saicone.pixelbuy.module.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BukkitCommandExecution {

    void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args);
}
