package com.minelatino.pixelbuy.command;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.command.sub.DatabaseCommand;
import com.minelatino.pixelbuy.command.sub.ReloadCommand;
import com.minelatino.pixelbuy.command.sub.WebdataCommand;
import com.minelatino.pixelbuy.util.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class MainCommand extends Command {

    private final PixelBuy pl = PixelBuy.get();

    private final DatabaseCommand databaseCommand = new DatabaseCommand(pl);
    private final ReloadCommand reloadCommand = new ReloadCommand(pl);
    private final WebdataCommand webdataCommand = new WebdataCommand(pl);

    public MainCommand(String cmd) {
        super(cmd);
        setAliases(Collections.singletonList("pbuy"));
    }

    @Override
    public boolean execute(CommandSender s, String label, String[] args) {
        if (!s.hasPermission(pl.configString("Perms.All")) || !s.hasPermission(pl.configString("Perms.Main"))) {
            s.sendMessage(Utils.color(pl.langString("Command.No-Perm")));
            return true;
        }
        if (args.length == 0) {
            pl.langStringList("Command.Help").forEach(string -> s.sendMessage(Utils.color(string)));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "database":
            case "db":
                if (s.hasPermission(pl.configString("Perms.Database")) || s.hasPermission(pl.configString("Perms.All"))) {
                    return databaseCommand.execute(s, args);
                } else {
                    s.sendMessage(Utils.color(pl.langString("Command.No-Perm")));
                    return true;
                }
            case "reload":
                if (s.hasPermission(pl.configString("Perms.Reload")) || s.hasPermission(pl.configString("Perms.All"))) {
                    return reloadCommand.execute(s, args);
                } else {
                    s.sendMessage(Utils.color(pl.langString("Command.No-Perm")));
                    return true;
                }
            case "webdata":
            case "orders":
            case "order:":
                if (s.hasPermission(pl.configString("Perms.Webdata")) || s.hasPermission(pl.configString("Perms.All"))) {
                    return webdataCommand.execute(s, args);
                } else {
                    s.sendMessage(Utils.color(pl.langString("Command.No-Perm")));
                    return true;
                }
            default:
                pl.langStringList("Command.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                return true;
        }
    }
}