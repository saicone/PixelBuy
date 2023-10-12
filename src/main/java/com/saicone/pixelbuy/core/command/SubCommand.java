package com.saicone.pixelbuy.core.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class SubCommand {

    @NotNull
    public abstract Pattern getAliases();

    @NotNull
    public abstract String getPermission();

    public void execute(@NotNull CommandSender sender, @NotNull String cmd, @NotNull String[] args) {
        // empty default method
    }
}