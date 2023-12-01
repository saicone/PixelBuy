package com.saicone.pixelbuy.core.command.sub;

import com.google.common.base.Enums;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelCommand;
import com.saicone.pixelbuy.module.hook.PlayerProvider;
import com.saicone.pixelbuy.util.MStrings;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OrderCommand extends PixelCommand {

    private static final String INDEX = MStrings.color("&6#- ");

    public OrderCommand() {
        super("order");
        subCommand("info", this::info);
        subCommand("fix", this::fix);
        subCommand("execute", 1, this::execution);
        subCommand("give", 2, this::give);
        subCommand("delete", this::delete);
        subCommand(new Item());
    }

    @Override
    public boolean main() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getSubStart() {
        return 1;
    }

    public void getOrder(@NotNull String s, @NotNull Function<StoreOrder, Boolean> consumer) {
        getOrder(s, false, consumer);
    }

    public void getOrder(@NotNull String s, boolean create, @NotNull Function<StoreOrder, Boolean> consumer) {
        final String[] split = s.split(":", 3);
        if (split.length < 2) {
            consumer.apply(null);
            return;
        }
        final String provider = split[0];
        if (!Strings.isNumber(split[1])) {
            consumer.apply(null);
            return;
        }
        final int id = Integer.parseInt(split[1]);
        final String group;
        if (split.length > 2) {
            group = split[2];
        } else {
            group = PixelBuy.get().getStore().getGroup();
        }

        for (var entry : PixelBuy.get().getDatabase().getCached().entrySet()) {
            if (entry.getValue().getOrders().isEmpty()) {
                continue;
            }
            final Iterator<StoreOrder> iterator = entry.getValue().getOrders().iterator();
            while (iterator.hasNext()) {
                final StoreOrder order = iterator.next();
                if (order.getProvider().equals(provider) && order.getId() == id && order.getGroup().equals(group)) {
                    if (consumer.apply(order) == null) {
                        iterator.remove();
                    }
                    return;
                }
            }
        }

        PixelBuy.get().getDatabase().getClient().getOrder(provider, id, group, order -> {
            final StoreOrder finalOrder;
            if (order == null && create) {
                finalOrder = new StoreOrder(provider, id, group);
            } else {
                finalOrder = order;
            }
            if (consumer.apply(finalOrder) == Boolean.TRUE && finalOrder != null) {
                PixelBuy.get().getDatabase().saveDataAsync(finalOrder, null);
            }
        });
    }

    public void getOrderAsync(@NotNull CommandSender sender, @NotNull String s, @NotNull Function<StoreOrder, Boolean> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> getOrder(s, order -> {
            if (order == null) {
                Lang.COMMAND_DISPLAY_ORDER_INVALID.sendTo(sender, s);
                return false;
            }
            return consumer.apply(order);
        }));
    }

    public void info(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        getOrderAsync(sender, cmd[cmd.length - 2], order -> {
            final String key = order.getProvider() + ":" + order.getId();
            final String saved = order.getDataId() > 0 ? Lang.TEXT_YES.getText(sender) : Lang.TEXT_NO.getText(sender);
            String buyer = order.getBuyer() != null ? Bukkit.getOfflinePlayer(order.getBuyer()).getName() : "<unknown>";
            if (buyer == null) {
                buyer = order.getBuyer().toString();
            }
            Lang.COMMAND_DISPLAY_ORDER_INFO.sendTo(sender, key, saved, order.getGroup(), buyer, order.getDate(), order.getExecution().name());
            boolean first = true;
            int cmdNum = 1;
            for (StoreOrder.Item item : order.getItems(PixelBuy.get().getStore().getGroup())) {
                for (String s : Lang.COMMAND_DISPLAY_ORDER_ITEM_INFO.getDisplay(sender)) {
                    s = Strings.replaceArgs(s, item.getId(), item.getAmount(), item.getPrice(), PixelBuy.get().getLang().getLangText(sender, "Order." + order.getExecution() + "." + item.getState()));
                    if (first) {
                        first = false;
                        sender.sendMessage(INDEX.replace("#", String.valueOf(cmdNum)) + s);
                    } else {
                        sender.sendMessage("   " + s);
                    }
                }
                first = true;
                cmdNum++;
            }
            return false;
        });
    }

    public void fix(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        getOrderAsync(sender, cmd[cmd.length - 2], order -> {
            boolean result = false;
            for (StoreOrder.Item item : order.getItems()) {
                if (item.getState() == StoreOrder.State.ERROR) {
                    item.state(StoreOrder.State.PENDING).error(null);
                    result = true;
                }
            }
            sendLang(sender, "Fix.Done", order.getKey());
            return result;
        });
    }

    public void execution(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        getOrderAsync(sender, cmd[cmd.length - 2], order -> {
            final StoreOrder.Execution execution = Enums.getIfPresent(StoreOrder.Execution.class, args[0]).orNull();
            if (execution == null) {
                sendLang(sender, "Execute.Invalid", args[0]);
                return false;
            }
            order.setExecution(execution);
            for (var entry : order.getAllItems().entrySet()) {
                for (StoreOrder.Item item : entry.getValue()) {
                    item.state(StoreOrder.State.PENDING);
                }
            }
            sendLang(sender, "Execute.Done", order.getKey(), args[0]);
            return true;
        });
    }

    public void give(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> getOrder(cmd[cmd.length - 2], true, order -> {
            if (order == null) {
                sendLang(sender, "Give.Format", cmd[cmd.length - 2]);
                return false;
            }
            if (order.getDataId() > 0) {
                sendLang(sender, "Give.Duplicated", cmd[cmd.length - 2]);
                return false;
            }
            final UUID buyer;
            if (args[0].length() < 21) {
                buyer = PlayerProvider.getUniqueId(args[0]);
            } else {
                try {
                    buyer = UUID.fromString(args[0]);
                } catch (IllegalArgumentException e) {
                    Lang.COMMAND_DISPLAY_USER_INVALID.sendTo(sender, args[0]);
                    return false;
                }
            }
            String group = order.getGroup();
            StoreOrder.State itemState = StoreOrder.State.PENDING;
            for (int i = 1; i < args.length; i++) {
                final String arg = args[i];
                if (arg.startsWith("--")) {
                    final String key = arg.substring(2, arg.indexOf('='));
                    final String value = arg.substring(arg.indexOf('=' + 1));
                    switch (key.toLowerCase().trim()) {
                        case "group":
                            group = value;
                            break;
                        case "date":
                            order.setDate(LocalDate.parse(value));
                            break;
                        case "execution":
                            order.setExecution(StoreOrder.Execution.valueOf(value.toUpperCase()));
                            break;
                        case "state":
                            itemState = StoreOrder.State.valueOf(value.toUpperCase());
                            break;
                    }
                } else {
                    final String[] split = arg.split("[|]");
                    var item = order.addItem(group, split[0]).state(itemState);
                    if (split.length > 1) {
                        try {
                            item.amount(Integer.parseInt(split[1]));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            order.setBuyer(buyer);
            if (PixelBuy.get().getStore().getCheckout().process(order)) {
                sendLang(sender, "Give.Done", order.getKey(), args[0]);
            }
            return false;
        }));
    }

    public void delete(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        getOrderAsync(sender, cmd[cmd.length - 2], order -> {
            PixelBuy.get().getDatabase().deleteDataAsync(order, () -> sendLang(sender, "Delete.Done", order.getKey()));
            return null;
        });
    }

    public class Item extends PixelCommand {
        public Item() {
            super("item");
            subCommand("info", this::info);
            subCommand("state", 1, this::state);
            subCommand("price", 1, this::price);
            subCommand("add", 1, this::add);
        }

        @Override
        public int getMinArgs() {
            return 2;
        }

        @Override
        public int getSubStart() {
            return 1;
        }

        public void getItemAsync(@NotNull CommandSender sender, @NotNull String key, @NotNull String s, @NotNull BiFunction<StoreOrder, StoreOrder.Item, Boolean> consumer) {
            getItemAsync(sender, key, s, false, consumer);
        }

        public void getItemAsync(@NotNull CommandSender sender, @NotNull String key, @NotNull String s, boolean create, @NotNull BiFunction<StoreOrder, StoreOrder.Item, Boolean> consumer) {
            getOrderAsync(sender, key, order -> {
                final String[] split = s.split(":", 2);
                final String group;
                final String id;
                if (split.length > 1) {
                    group = split[0];
                    id = split[1];
                } else {
                    group = PixelBuy.get().getStore().getGroup();
                    id = s;
                }
                for (StoreOrder.Item item : order.getItems(group)) {
                    if (item.getId().equals(id)) {
                        return consumer.apply(order, item);
                    }
                }
                if (create) {
                    return consumer.apply(order, order.addItem(group, id));
                }
                Lang.COMMAND_DISPLAY_ORDER_ITEM_INVALID.sendTo(sender, key, s);
                return false;
            });
        }

        public void info(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
            getItemAsync(sender, cmd[cmd.length - 4], cmd[cmd.length - 2], (order, item) -> {
                if (args.length > 0 && args[0].equalsIgnoreCase("error")) {
                    sendLang(sender, "Info.Error", item.getId(), String.valueOf(item.getError()));
                } else {
                    Lang.COMMAND_DISPLAY_ORDER_ITEM_INFO.sendTo(sender, item.getId(), item.getAmount(), item.getPrice(), PixelBuy.get().getLang().getLangText(sender, "Order." + order.getExecution() + "." + item.getState()));
                }
                return false;
            });
        }

        public void state(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
            final StoreOrder.State state = Enums.getIfPresent(StoreOrder.State.class, args[0]).orNull();
            if (state == null) {
                sendLang(sender, "State.Invalid", args[0]);
                return;
            }
            getItemAsync(sender, cmd[cmd.length - 4], cmd[cmd.length - 2], (order, item) -> {
                item.state(state).error(args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null);
                sendLang(sender, "State.Done", state.name());
                return true;
            });
        }

        public void price(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
            if (!Strings.isNumber(args[0]) || args[0].startsWith("-")) {
                sendLang(sender, "Price.Invalid", args[0]);
                return;
            }
            getItemAsync(sender, cmd[cmd.length - 4], cmd[cmd.length - 2], (order, item) -> {
                item.price(Float.parseFloat(args[0]));
                sendLang(sender, "Price.Done", item.getPrice());
                return true;
            });
        }

        public void add(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
            getItemAsync(sender, cmd[cmd.length - 4], cmd[cmd.length - 2], true, (order, item) -> {
                final StoreOrder.State state = args.length > 0 ? Enums.getIfPresent(StoreOrder.State.class, args[0]).or(item.getState()) : item.getState();
                final float price = args.length > 1 && Strings.isNumber(args[1]) && args[1].startsWith("-") ? Float.parseFloat(args[1]) : 0.0f;
                final String error = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;
                item.state(state).price(price).error(error);
                sendLang(sender, "Add.Done", item.getId(), order.getKey());
                return true;
            });
        }
    }
}