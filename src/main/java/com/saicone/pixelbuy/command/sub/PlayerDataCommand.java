package com.saicone.pixelbuy.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.command.SubCommand;
import com.saicone.pixelbuy.managers.player.PlayerData;
import com.saicone.pixelbuy.util.Utils;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PlayerDataCommand extends SubCommand {

    private final PixelBuy pl = PixelBuy.get();

    @Override
    public Pattern getAliases() {
        return Pattern.compile("(p(layer)?)?data");
    }

    @Override
    public String getPermission() {
        return pl.getFiles().getConfig().getString("Perms.PlayerData", "pixelbuy.playerdata");
    }

    @Override
    public void execute(CommandSender sender, String cmd, String[] args) {
        if (args.length == 1) {
            pl.langStringList("Command.Playerdata.Help").forEach(string -> sender.sendMessage(Utils.color(string.replace("%cmd%", cmd))));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "info":
                if (args.length <= 2) {
                    sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Info.Use").replace("%cmd%", cmd)));
                } else {
                    PlayerData data = pl.getPlayerManager().getPlayerData(args[2]);
                    if (data == null) {
                        sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Info.Not-Have")));
                        break;
                    }

                    int page = Math.max(1, (args.length > 3 ? Integer.parseInt(args[3]) : 1)) - 1;
                    final List<PlayerData.Order> orders = data.getOrders();
                    final int max = page * 10 + 10;

                    sender.sendMessage(" ");
                    pl.langStringList("Command.Playerdata.Info.Player").forEach(string -> sender.sendMessage(Utils.color(string)
                            .replace("%player%", data.getPlayer())
                            .replace("%donated%", String.valueOf(data.getDonated()))
                            .replace("%page%", String.valueOf(page + 1))
                            .replace("%maxpage%", String.valueOf((orders.size() - 1) / 10 + 1))));
                    int orderNum = 1;
                    for (int i = page * 10; i < orders.size() && i < max; i++) {
                        final PlayerData.Order order = orders.get(i);
                        for (String string : pl.langStringList("Command.Playerdata.Info.Order")) {
                            sender.sendMessage(Utils.color(string).replace("%num%", String.valueOf(orderNum)).replace("%id%", String.valueOf(order.getId())));
                        }
                        int cmdNum = 1;
                        for (Map.Entry<String, Byte> item : order.getItems().entrySet()) {
                            sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Info.Items").replace("%num%", String.valueOf(cmdNum)).replace("%item%", item.getKey()).replace("%state%", state(item.getValue()))));
                            cmdNum++;
                        }
                        orderNum++;
                    }
                    sender.sendMessage(" ");
                    break;
                }
            case "refund":
                if (!sender.hasPermission("pixelbuy.playerdata.refund")) {
                    sender.sendMessage(Utils.color(pl.langString("Command.No-Perm")));
                } else if (args.length < 4) {
                    sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Refund.Use").replace("%cmd%", cmd)));
                } else {
                    if (pl.getPlayerManager().refundOrder(args[2], Integer.valueOf(args[3]))) {
                        sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Refund.Done").replace("%player%", args[2]).replace("%order%", args[3])));
                    } else {
                        sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Refund.Error")));
                    }
                }
                break;
            case "false-order":
            case "order":
                if (!sender.hasPermission("pixelbuy.playerdata.falseorder")) {
                    sender.sendMessage(Utils.color(pl.langString("Command.No-Perm")));
                } else if (args.length < 5) {
                    sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Order.Use").replace("%cmd%", cmd)));
                } else {
                    Map<String, Byte> items = new HashMap<>();
                    for (String item : args[4].split(",")) {
                        items.put(item, (byte) 1);
                    }
                    pl.getPlayerManager().processOrder(args[2], new PlayerData.Order(Integer.valueOf(args[3]), items));
                    sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Order.Done").replace("%orderID%", args[3]).replace("%player%", args[2]).replace("%items%", args[4])));
                }
                break;
            case "recover-order":
            case "recover":
                if (!sender.hasPermission("pixelbuy.playerdata.recover")) {
                    sender.sendMessage(Utils.color(pl.langString("Command.No-Perm")));
                } else if (args.length < 4) {
                    sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Recover.Use", "&6Debes usar &e/%cmd% player recover <jugador> <id de compra>").replace("%cmd%", cmd)));
                } else {
                    PlayerData data = pl.getPlayerManager().getPlayerData(args[2]);
                    if (data == null) {
                        sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Info.Not-Have")));
                        break;
                    }

                    final PlayerData.Order order = data.getOrder(Integer.parseInt(args[3]));
                    if (order == null) {
                        sender.sendMessage(Utils.color(pl.langString("Command.Playerdata.Recover.Not-Found", "&cLa orden de compra con el ID &6%orderID% &cno existe")));
                        break;
                    }

                    final List<String> list = new ArrayList<>();
                    for (Map.Entry<String, Byte> entry : order.getItems((byte) 2).entrySet()) {
                        boolean hasRecovered = entry.getKey().endsWith("-copy");
                        if (hasRecovered || pl.getStore().isItem(entry.getKey() + "-copy")) {
                            list.add(hasRecovered ? entry.getKey().substring(0, entry.getKey().length() - 5) : entry.getKey());
                            if (hasRecovered) {
                                order.getItems().put(entry.getKey(), (byte) 1);
                            } else {
                                order.getItems().remove(entry.getKey());
                                order.getItems().put(entry.getKey() + "-copy", (byte) 1);
                            }
                        }
                    }

                    pl.getPlayerManager().saveDataChanges(args[2], data);
                    sender.sendMessage(Utils.color(
                            pl.langString("Command.Playerdata.Recover.Done", "&aSe recuperó la orden &f%orderID% &adel jugador &f%player% &acon los items de compra &f%items%")
                                    .replace("%orderID%", args[3])
                                    .replace("%player%", args[2])
                                    .replace("%items%", list.isEmpty() ? "- vacio -" : String.join(", ", list))
                    ));
                }
                break;
            default:
                pl.langStringList("Command.Playerdata.Help").forEach(string -> sender.sendMessage(Utils.color(string.replace("%cmd%", cmd))));
                break;
        }
    }

    private String state(Byte state) {
        if (state == 1) return pl.langString("Messages.Pending");
        if (state == 2) return pl.langString("Messages.Sent");
        return pl.langString("Messages.Refunded");
    }
}
