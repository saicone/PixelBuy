package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.SubCommand;
import com.saicone.pixelbuy.api.object.StoreUser;

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
        return PixelBuy.settings().getString("Perms.PlayerData", "pixelbuy.playerdata");
    }

    @Override
    public void execute(CommandSender sender, String cmd, String[] args) {
        if (args.length == 1) {
            Lang.COMMAND_PLAYERDATA_HELP.sendTo(sender, cmd);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "info":
                if (args.length <= 2) {
                    Lang.COMMAND_PLAYERDATA_INFO_USAGE.sendTo(sender, cmd);
                } else {
                    StoreUser data = pl.getPlayerManager().getPlayerData(args[2]);
                    if (data == null) {
                        Lang.COMMAND_PLAYERDATA_INFO_UNKNOWN.sendTo(sender);
                        break;
                    }

                    int page = Math.max(1, (args.length > 3 ? Integer.parseInt(args[3]) : 1)) - 1;
                    final List<StoreUser.Order> orders = data.getOrders();
                    final int max = page * 10 + 10;

                    sender.sendMessage(" ");
                    Lang.COMMAND_PLAYERDATA_INFO_PLAYER.sendTo(sender, data.getPlayer(), data.getDonated(), page + 1, (orders.size() - 1) / 10 + 1);
                    int orderNum = 1;
                    for (int i = page * 10; i < orders.size() && i < max; i++) {
                        final StoreUser.Order order = orders.get(i);
                        Lang.COMMAND_PLAYERDATA_INFO_ORDER.sendTo(sender, orderNum, order.getId());
                        int cmdNum = 1;
                        for (Map.Entry<String, Byte> item : order.getItems().entrySet()) {
                            Lang.COMMAND_PLAYERDATA_INFO_ITEMS.sendTo(sender, cmdNum, item.getKey(), state(sender, item.getValue()));
                            cmdNum++;
                        }
                        orderNum++;
                    }
                    sender.sendMessage(" ");
                    break;
                }
            case "refund":
                if (!sender.hasPermission("pixelbuy.playerdata.refund")) {
                    Lang.COMMAND_NO_PERM.sendTo(sender);
                } else if (args.length < 4) {
                    Lang.COMMAND_PLAYERDATA_REFUND_USAGE.sendTo(sender, cmd);
                } else {
                    if (pl.getPlayerManager().refundOrder(args[2], Integer.valueOf(args[3]))) {
                        Lang.COMMAND_PLAYERDATA_REFUND_DONE.sendTo(sender, args[2], args[3]);
                    } else {
                        Lang.COMMAND_PLAYERDATA_REFUND_ERROR.sendTo(sender);
                    }
                }
                break;
            case "false-order":
            case "order":
                if (!sender.hasPermission("pixelbuy.playerdata.falseorder")) {
                    Lang.COMMAND_NO_PERM.sendTo(sender);
                } else if (args.length < 5) {
                    Lang.COMMAND_PLAYERDATA_ORDER_USAGE.sendTo(sender, cmd);
                } else {
                    Map<String, Byte> items = new HashMap<>();
                    for (String item : args[4].split(",")) {
                        items.put(item, (byte) 1);
                    }
                    pl.getPlayerManager().processOrder(args[2], new StoreUser.Order(Integer.valueOf(args[3]), items));
                    Lang.COMMAND_PLAYERDATA_ORDER_DONE.sendTo(sender, args[3], args[2], args[4]);
                }
                break;
            case "recover-order":
            case "recover":
                if (!sender.hasPermission("pixelbuy.playerdata.recover")) {
                    Lang.COMMAND_NO_PERM.sendTo(sender);
                } else if (args.length < 4) {
                    Lang.COMMAND_PLAYERDATA_RECOVER_USAGE.sendTo(sender, cmd);
                } else {
                    StoreUser data = pl.getPlayerManager().getPlayerData(args[2]);
                    if (data == null) {
                        Lang.COMMAND_PLAYERDATA_INFO_UNKNOWN.sendTo(sender, args[2]);
                        break;
                    }

                    final StoreUser.Order order = data.getOrder(Integer.parseInt(args[3]));
                    if (order == null) {
                        Lang.COMMAND_PLAYERDATA_RECOVER_UNKNOWN.sendTo(sender, args[3]);
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
                    Lang.COMMAND_PLAYERDATA_RECOVER_DONE.sendTo(sender, args[3], args[2],  list.isEmpty() ? "- nothing -" : String.join(", ", list));
                }
                break;
            default:
                Lang.COMMAND_PLAYERDATA_HELP.sendTo(sender, cmd);
                break;
        }
    }

    private String state(CommandSender sender, Byte state) {
        if (state == 1) return Lang.STATUS_PENDING.getText(sender);
        if (state == 2) return Lang.STATUS_SENT.getText(sender);
        return Lang.STATUS_REFUNDED.getText(sender);
    }
}
