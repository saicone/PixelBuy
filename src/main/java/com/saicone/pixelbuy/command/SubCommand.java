package com.saicone.pixelbuy.command;

import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public abstract class SubCommand {

    public abstract Pattern getAliases();

    public abstract String getPermission();

    public void execute(CommandSender sender, String cmd, String[] args) { }
}