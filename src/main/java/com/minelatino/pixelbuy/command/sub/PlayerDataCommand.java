package com.minelatino.pixelbuy.command.sub;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import com.minelatino.pixelbuy.util.Utils;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class PlayerDataCommand {

    private final PixelBuy pl;

    public PlayerDataCommand(PixelBuy pl) {
        this.pl = pl;
    }

    public boolean execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            pl.langStringList("Command.Playerdata.Help").forEach(string -> s.sendMessage(Utils.color(string)));
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "info":
                if (args.length == 2) {
                    s.sendMessage(Utils.color(pl.langString("Command.Playerdata.Info.Use")));
                } else {
                    PlayerData data = pl.getPlayerManager().getPlayerData(args[2]);
                    if (data == null) {
                        s.sendMessage(Utils.color(pl.langString("Command.Playerdata.Info.Not-Have")));
                        return true;
                    }
                    s.sendMessage(" ");
                    pl.langStringList("Command.Playerdata.Info.Player").forEach(string -> s.sendMessage(Utils.color(string).replace("%player%", data.getPlayer()).replace("%donated%", String.valueOf(data.getDonated()))));
                    int orderNum = 1;
                    for (PlayerData.Order order : data.getOrders()) {
                        for (String string : pl.langStringList("Command.Playerdata.Info.Order")) {
                            s.sendMessage(Utils.color(string).replace("%num%", String.valueOf(orderNum)).replace("%id%", String.valueOf(order.getId())));
                        }
                        int cmdNum = 1;
                        for (Map.Entry<String, Byte> item : order.getItems().entrySet()) {
                            s.sendMessage(Utils.color(pl.langString("Command.Playerdata.Info.Items").replace("%num%", String.valueOf(cmdNum)).replace("%item%", item.getKey()).replace("%state%", state(item.getValue()))));
                            cmdNum++;
                        }
                        orderNum++;
                    }
                    s.sendMessage(" ");
                    return true;
                }
            case "refund":
                if (args.length < 4) {
                    s.sendMessage(Utils.color(pl.langString("Command.Playerdata.Refund.Use")));
                } else {
                    if (pl.getPlayerManager().refundOrder(args[2], Integer.valueOf(args[3]))) {
                        s.sendMessage(Utils.color(pl.langString("Command.Playerdata.Refund.Done").replace("%player%", args[2]).replace("%order%", args[3])));
                    } else {
                        s.sendMessage(Utils.color(pl.langString("Command.Playerdata.Refund.Error")));
                    }
                }
                return true;
            case "false-order":
            case "order":
                if (args.length < 5) {
                    s.sendMessage(Utils.color(pl.langString("Command.Playerdata.Order.Use")));
                } else {
                    Map<String, Byte> items = new HashMap<>();
                    for (String item : args[4].split(",")) {
                        items.put(item, (byte) 1);
                    }
                    pl.getPlayerManager().processOrder(args[2], new PlayerData.Order(Integer.valueOf(args[3]), items));
                    s.sendMessage(Utils.color(pl.langString("Command.Playerdata.Order.Done").replace("%orderID%", args[3]).replace("%player%", args[2]).replace("%items%", args[4])));
                }
                return true;
            default:
                pl.langStringList("Command.Playerdata.Help").forEach(string -> s.sendMessage(Utils.color(string)));
                return true;
        }
    }

    private String state(Byte state) {
        if (state == 1) return pl.langString("Messages.Pending");
        if (state == 2) return pl.langString("Messages.Sent");
        return pl.langString("Messages.Refunded");
    }
}
