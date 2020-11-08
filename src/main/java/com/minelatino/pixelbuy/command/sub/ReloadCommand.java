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
            pl.getFiles().getLang().getStringList("Command.Reload.Help").forEach(string -> s.sendMessage(Utils.color(string)));
            return true;
        }
        switch (args[1].toLowerCase()) {
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
                    s.sendMessage(Utils.color(pl.getFiles().getLang().getString("Command.Reload.Files.Use")));
                    return true;
                }
                pl.getFiles().reloadSettings(s, false);
                pl.getFiles().reloadLang(s, pl.getFiles().getConfig().getString("Language"));
                return true;
            case "database":
                pl.getDatabase().reload(s);
                return true;
            case "webdata":
                pl.getOrderManager().reload(false);
                s.sendMessage(Utils.color(pl.getFiles().getLang().getString("Command.Reload.Webdata.Success")));
                return true;
            case "all":
                pl.getFiles().reloadSettings(s, false);
                pl.getFiles().reloadLang(s, pl.getFiles().getConfig().getString("Language"));
                pl.getDatabase().reload(s);
                pl.getOrderManager().reload(false);
                s.sendMessage(Utils.color(pl.getFiles().getLang().getString("Command.Reload.Webdata.Success")));
                return true;
            default:
                pl.getFiles().getLang().getStringList("Command.Reload.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                return true;
        }
    }
}
