package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.SubCommand;
import com.saicone.pixelbuy.api.store.StoreUser;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

public class PlayerDataCommand extends SubCommand {

    private final PixelBuy plugin = PixelBuy.get();

    @Override
    public @NotNull Pattern getAliases() {
        return Pattern.compile("(p(layer)?)?data");
    }

    @Override
    public @NotNull String getPermission() {
        return PixelBuy.settings().getString("Perms.PlayerData", "pixelbuy.playerdata");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String cmd, @NotNull String[] args) {
        if (args.length == 1) {
            Lang.COMMAND_PLAYERDATA_HELP.sendTo(sender, cmd);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "info":
                if (args.length <= 2) {
                    Lang.COMMAND_PLAYERDATA_INFO_USAGE.sendTo(sender, cmd);
                } else {
                    final StoreUser user = plugin.getUserCore().getPlayerData(args[2]);
                    if (user == null) {
                        Lang.COMMAND_PLAYERDATA_INFO_UNKNOWN.sendTo(sender);
                        break;
                    }

                    final int page = Math.max(1, (args.length > 3 ? Integer.parseInt(args[3]) : 1)) - 1;
                    final Set<StoreOrder> orders = user.getOrders();

                    sender.sendMessage(" ");
                    Lang.COMMAND_PLAYERDATA_INFO_PLAYER.sendTo(sender, user.getName(), user.getDonatedOld(), page + 1, (orders.size() - 1) / 10 + 1);
                    int orderNum = 1;
                    int start = page * 10;
                    int i = 0;
                    for (StoreOrder order : orders) {
                        if (i < start) {
                            i++;
                            continue;
                        }
                        if (orderNum >= 10) {
                            break;
                        }
                        Lang.COMMAND_PLAYERDATA_INFO_ORDER.sendTo(sender, orderNum, order.getId());
                        int cmdNum = 1;
                        for (StoreOrder.Item item : order.getItems()) {
                            Lang.COMMAND_PLAYERDATA_INFO_ITEMS.sendTo(sender, cmdNum, item.getId(), state(sender, order, item));
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
                    if (plugin.getUserCore().refundOrder(args[2], Integer.parseInt(args[3]))) {
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
                    final StoreOrder order = new StoreOrder("command", Integer.parseInt(args[3]), PixelBuy.get().getStore().getGroup());
                    for (String item : args[4].split(",")) {
                        order.addItem(item);
                    }
                    plugin.getUserCore().processOrder(args[2], order, false);
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
                    final StoreUser user = plugin.getUserCore().getPlayerData(args[2]);
                    if (user == null) {
                        Lang.COMMAND_PLAYERDATA_INFO_UNKNOWN.sendTo(sender, args[2]);
                        break;
                    }

                    final StoreOrder order = user.getOrder(Integer.parseInt(args[3]));
                    if (order == null) {
                        Lang.COMMAND_PLAYERDATA_RECOVER_UNKNOWN.sendTo(sender, args[3]);
                        break;
                    }

                    order.setExecution(StoreOrder.Execution.RECOVER);
                    final List<String> list = new ArrayList<>();
                    for (StoreOrder.Item item : order.getItems()) {
                        if (item.getState() == StoreOrder.State.PENDING) {
                            continue;
                        }
                        item.state(StoreOrder.State.PENDING);
                        list.add(item.getId());
                    }

                    plugin.getUserCore().saveDataChanges(args[2], user);
                    Lang.COMMAND_PLAYERDATA_RECOVER_DONE.sendTo(sender, args[3], args[2],  list.isEmpty() ? "- nothing -" : String.join(", ", list));
                }
                break;
            default:
                Lang.COMMAND_PLAYERDATA_HELP.sendTo(sender, cmd);
                break;
        }
    }

    @NotNull
    private String state(@NotNull CommandSender sender, @NotNull StoreOrder order, @NotNull StoreOrder.Item item) {
        if (item.getState() == StoreOrder.State.DONE) {
            return Lang.STATUS_SENT.getText(sender);
        }
        if (item.getState() == StoreOrder.State.PENDING) {
            return Lang.STATUS_PENDING.getText(sender);
        }
        if (order.getExecution() == StoreOrder.Execution.REFUND) {
            return Lang.STATUS_REFUNDED.getText(sender);
        }
        return "";
    }
}
