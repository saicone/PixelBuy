package com.minelatino.pixelbuy.command.sub;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.util.Utils;

import org.bukkit.command.CommandSender;

public class ReloadCommand {

    private final PixelBuy pl;

    public ReloadCommand(PixelBuy pl) {
        this.pl = pl;
    }

    public boolean execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            pl.langStringList("Command.Reload.Help").forEach(string -> s.sendMessage(Utils.color(string)));
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "file":
            case "files":
                if (args.length == 3) {
                    if (args[2].toLowerCase().equals("settings")) {
                        pl.getFiles().reloadSettings(s, false);
                        return true;
                    }
                    if (args[2].toLowerCase().equals("messages")) {
                        pl.getFiles().reloadLang(s, pl.getFiles().getConfig().getString("Language"));
                        return true;
                    }
                    s.sendMessage(Utils.color(pl.langString("Command.Reload.Files.Use")));
                    return true;
                }
                pl.getFiles().reloadSettings(s, false);
                pl.getFiles().reloadLang(s, pl.getFiles().getConfig().getString("Language"));
                return true;
            case "store":
                pl.getStore().reload(s, false);
                return true;
            case "database":
                pl.getDatabase().reload(s);
                return true;
            case "webdata":
                pl.getOrderManager().reload(false);
                s.sendMessage(Utils.color(pl.langString("Command.Reload.Webdata.Success")));
                return true;
            case "all":
                pl.getFiles().reloadSettings(s, false);
                pl.getFiles().reloadLang(s, pl.getFiles().getConfig().getString("Language"));
                pl.getStore().reload(s, false);
                pl.getDatabase().reload(s);
                pl.getOrderManager().reload(false);
                s.sendMessage(Utils.color(pl.langString("Command.Reload.Webdata.Success")));
                return true;
            default:
                pl.langStringList("Command.Reload.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                return true;
        }
    }
}
