package com.minelatino.pixelbuy.command.sub;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.command.CommandSender;

public class DatabaseCommand {

    private final PixelBuy pl;

    public DatabaseCommand(PixelBuy pl) {
        this.pl = pl;
    }

    public boolean execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            pl.LANG.getStringList("Command.Database.Help").forEach(string -> s.sendMessage(Utils.color(string)));
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "convert":
                if (args.length == 2) {
                    s.sendMessage(Utils.color(pl.LANG.getString("Command.Database.Convert.Use")));
                } else if (args.length == 3) {
                    pl.getDatabase().convertData(s, args[2], false);
                } else {
                    pl.getDatabase().convertData(s, args[2], Boolean.getBoolean(args[3]));
                }
                return true;
            case "delete":
                if (args.length == 2) {
                    s.sendMessage(Utils.color(pl.LANG.getString("Command.Database.Delete.Use")));
                } else {
                    pl.getDatabase().deleteData(args[2]);
                    s.sendMessage(Utils.color(pl.LANG.getString("Command.Database.Delete.Success").replace("%player%", args[2])));
                }
                return true;
            default:
                pl.LANG.getStringList("Command.Database.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                return true;
        }
    }
}
