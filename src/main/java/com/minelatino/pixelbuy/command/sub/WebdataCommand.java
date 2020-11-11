package com.minelatino.pixelbuy.command.sub;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.player.Order;
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
                    pl.langStringList("Command.Webdata.Info.Player").forEach(string -> s.sendMessage(Utils.color(string).replace("%player%", data.getPlayer())));
                    int orderNum = 1;
                    for (Order order : data.getOrders()) {
                        for (String string : pl.langStringList("Command.Webdata.Info.Order")) {
                            s.sendMessage(Utils.color(string).replace("%num%", String.valueOf(orderNum)).replace("%id%", String.valueOf(order.getId())));
                        }
                        int cmdNum = 1;
                        for (String cmd : order.getCmds()) {
                            s.sendMessage(Utils.color(pl.langString("Command.Webdata.Info.Cmds").replace("%num%", String.valueOf(cmdNum)).replace("%cmd%", String.valueOf(cmd))));
                            cmdNum++;
                        }
                        orderNum++;
                    }
                    s.sendMessage(" ");
                    return true;
                }
            default:
                pl.getFiles().getLang().getStringList("Command.Webdata.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                return true;
        }
    }
}
