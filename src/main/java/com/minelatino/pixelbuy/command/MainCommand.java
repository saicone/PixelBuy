package com.minelatino.pixelbuy.command;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.command.sub.DatabaseCommand;
import com.minelatino.pixelbuy.command.sub.ReloadCommand;
import com.minelatino.pixelbuy.command.sub.WebdataCommand;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {

    private final PixelBuy pl = PixelBuy.get();

    private final DatabaseCommand databaseCommand = new DatabaseCommand(pl);
    private final ReloadCommand reloadCommand = new ReloadCommand(pl);
    private final WebdataCommand webdataCommand = new WebdataCommand(pl);

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!s.hasPermission(pl.SETTINGS.getString("Perms.All")) || !s.hasPermission(pl.SETTINGS.getString("Perms.Main"))) {
            s.sendMessage(Utils.color(pl.LANG.getString("Command.No-Perm")));
            return true;
        }
        if (args.length == 0) {
            pl.LANG.getStringList("Command.Help").forEach(string -> s.sendMessage(Utils.color(string)));
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "database":
            case "db":
                if (s.hasPermission(pl.SETTINGS.getString("Perms.Database")) || s.hasPermission(pl.SETTINGS.getString("Perms.All"))) {
                    return databaseCommand.execute(s, args);
                } else {
                    s.sendMessage(Utils.color(pl.LANG.getString("Command.No-Perm")));
                    return true;
                }
            case "reload":
                if (s.hasPermission(pl.SETTINGS.getString("Perms.Reload")) || s.hasPermission(pl.SETTINGS.getString("Perms.All"))) {
                    return reloadCommand.execute(s, args);
                } else {
                    s.sendMessage(Utils.color(pl.LANG.getString("Command.No-Perm")));
                    return true;
                }
            case "webdata":
            case "orders":
            case "order:":
                if (s.hasPermission(pl.SETTINGS.getString("Perms.Webdata")) || s.hasPermission(pl.SETTINGS.getString("Perms.All"))) {
                    return webdataCommand.execute(s, args);
                } else {
                    s.sendMessage(Utils.color(pl.LANG.getString("Command.No-Perm")));
                    return true;
                }
            default:
                pl.LANG.getStringList("Command.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                return true;
        }
    }
}