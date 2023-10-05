package com.saicone.pixelbuy.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.command.SubCommand;
import com.saicone.pixelbuy.util.Utils;

import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class DatabaseCommand extends SubCommand {

    private final PixelBuy pl = PixelBuy.get();

    @Override
    public Pattern getAliases() {
        return Pattern.compile("d(ata)?b(ase)?");
    }

    @Override
    public String getPermission() {
        return pl.getFiles().getConfig().getString("Perms.Database", "pixelbuy.database");
    }

    @Override
    public void execute(CommandSender sender, String cmd, String[] args) {
        if (args.length == 1) {
            pl.langStringList("Command.Database.Help").forEach(string -> sender.sendMessage(Utils.color(string.replace("%cmd%", cmd))));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "convert":
                if (args.length == 2) {
                    sender.sendMessage(Utils.color(pl.langString("Command.Database.Convert.Use").replace("%cmd%", cmd)));
                } else if (args.length == 3) {
                    pl.getDatabase().convertData(sender, args[2], false);
                } else {
                    pl.getDatabase().convertData(sender, args[2], Boolean.getBoolean(args[3]));
                }
                break;
            case "delete":
                if (args.length == 2) {
                    sender.sendMessage(Utils.color(pl.langString("Command.Database.Delete.Use").replace("%cmd%", cmd)));
                } else {
                    pl.getDatabase().deleteData(args[2]);
                    sender.sendMessage(Utils.color(pl.langString("Command.Database.Delete.Success").replace("%player%", args[2])));
                }
                break;
            default:
                pl.langStringList("Command.Database.Help").forEach(string -> sender.sendMessage(Utils.color(string.replace("%cmd%", cmd))));
        }
    }
}
