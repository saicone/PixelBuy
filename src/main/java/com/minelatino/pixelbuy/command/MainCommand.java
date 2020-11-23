package com.minelatino.pixelbuy.command;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.command.sub.DatabaseCommand;
import com.minelatino.pixelbuy.command.sub.ReloadCommand;
import com.minelatino.pixelbuy.command.sub.PlayerDataCommand;
import com.minelatino.pixelbuy.command.sub.StoreCommand;
import com.minelatino.pixelbuy.util.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class MainCommand extends Command {

    private final PixelBuy pl = PixelBuy.get();

    private final DatabaseCommand databaseCommand = new DatabaseCommand(pl);
    private final PlayerDataCommand playerDataCommand = new PlayerDataCommand(pl);
    private final ReloadCommand reloadCommand = new ReloadCommand(pl);
    private final StoreCommand storeCommand = new StoreCommand(pl);

    public MainCommand(String cmd) {
        super(cmd);
        setAliases(Collections.singletonList("pbuy"));
    }

    @Override
    public boolean execute(CommandSender s, String label, String[] args) {
        if (!hasPerm(s, "Perms.Main")) return true;
        if (args.length == 0) {
            pl.langStringList("Command.Help").forEach(string -> s.sendMessage(Utils.color(string)));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "database":
            case "db":
                if (hasPerm(s, "Perms.Database")) return databaseCommand.execute(s, args);
                return true;
            case "playerdata":
            case "player":
            case "data":
                if (hasPerm(s, "Perms.PlayerData")) return playerDataCommand.execute(s, args);
                return true;
            case "reload":
                if (hasPerm(s, "Perms.Reload")) return reloadCommand.execute(s, args);
                return true;
            case "status":
                if (hasPerm(s, "Perms.Status")) {
                    pl.langStringList("Command.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                }
                return true;
            case "store":
                if (hasPerm(s, "Perms.Store")) return storeCommand.execute(s, args);
                return true;
            default:
                pl.langStringList("Command.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                return true;
        }
    }

    public boolean hasPerm(CommandSender sender, String path) {
        if (sender.hasPermission(pl.configString(path)) || sender.hasPermission(pl.configString("Perms.All"))) return true;
        sender.sendMessage(Utils.color(pl.langString("Command.No-Perm")));
        return false;
    }
}