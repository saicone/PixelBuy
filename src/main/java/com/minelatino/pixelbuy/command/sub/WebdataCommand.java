package com.minelatino.pixelbuy.command.sub;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.command.CommandSender;

public class WebdataCommand {

    private final PixelBuy pl;

    public WebdataCommand(PixelBuy pl) {
        this.pl = pl;
    }

    public boolean execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            pl.getFiles().getLang().getStringList("Command.Webdata.Help").forEach(string -> s.sendMessage(Utils.color(string)));
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "check":
                pl.getOrderManager().checkWebData(s);
                return true;
            case "info":
                if (args.length == 2) {
                    s.sendMessage(Utils.color(pl.getFiles().getLang().getString("Command.Webdata.Info.Use")));
                } else {
                    PlayerData data = pl.getDatabase().getData(args[2]);
                    if (data == null) {
                        s.sendMessage(Utils.color(pl.getFiles().getLang().getString("Command.Webdata.Info.Not-Have")));
                        return true;
                    }
                    s.sendMessage(" ");
                    s.sendMessage(Utils.color(pl.getFiles().getLang().getString("Command.Webdata.Info.Player").replace("%player%", data.getPlayer())));
                    data.getCommands().forEach(cmd -> s.sendMessage(Utils.color(pl.getFiles().getLang().getString("Command.Webdata.Info.Cmds").replace("%cmd%", cmd))));
                    s.sendMessage(" ");
                    return true;
                }
            default:
                pl.getFiles().getLang().getStringList("Command.Webdata.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                return true;
        }
    }
}
