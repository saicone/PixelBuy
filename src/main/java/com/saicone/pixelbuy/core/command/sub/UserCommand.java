package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelCommand;
import com.saicone.pixelbuy.api.store.StoreUser;

import com.saicone.pixelbuy.module.hook.PlayerProvider;
import com.saicone.pixelbuy.util.MStrings;
import com.saicone.pixelbuy.util.OptionalType;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class UserCommand extends PixelCommand {

    private static final String INDEX = MStrings.color("&6#- ");
    private static final int PAGE_SIZE = 5;

    public UserCommand() {
        super("user");
        subCommand("info", this::info);
        subCommand("calculate", this::calculate);
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

    @Nullable
    public static StoreUser getUser(@NotNull String s) {
        final StoreUser user;
        if (s.length() < 21) {
            user = PixelBuy.get().getDatabase().getDataOrNull(PlayerProvider.getUniqueId(s), s);
        } else {
            try {
                final UUID id = UUID.fromString(s);
                final String name = PlayerProvider.getName(id);
                if (name == null) {
                    return null;
                }
                user = PixelBuy.get().getDatabase().getDataOrNull(id, name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        if (user == null) {
            return null;
        }
        if (!user.isLoaded()) {
            PixelBuy.get().getDatabase().loadOrders(true, user);
        }
        return user;
    }

    public void getUserAsync(@NotNull CommandSender sender, @NotNull String s, @NotNull Consumer<StoreUser> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> {
            final StoreUser user = getUser(s);
            if (user == null) {
                Lang.COMMAND_DISPLAY_USER_INVALID.sendTo(sender, s);
                return;
            }
            consumer.accept(user);
        });
    }

    public void info(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        getUserAsync(sender, cmd[cmd.length - 2], (user) -> {
            final boolean numArg = args.length > 0 && Strings.isNumber(args[0]);
            final int page = numArg ? OptionalType.of(args[0]).asInt(-1) : 1;
            if (page < 1) {
                getSubCommand("info").sendUsage(sender, cmd, args);
                return;
            }
            final String currentGroup = args.length > 1 ? args[1] : (numArg || args.length < 1 ? PixelBuy.get().getStore().getGroup() : args[0]);

            final Set<StoreOrder> orders = user.getOrders();
            Lang.COMMAND_DISPLAY_USER_INFO.sendTo(sender, user.getUniqueId(), user.getName(), user.getDonated(), page, (orders.size() - 1) / PAGE_SIZE + 1);
            int orderNum = 1;
            int start = (page - 1) * PAGE_SIZE;
            int i = 0;
            for (StoreOrder order : orders) {
                if (i < start) {
                    i++;
                    continue;
                }
                if (orderNum > PAGE_SIZE) {
                    break;
                }
                final String key = order.getProvider() + ":" + order.getId();
                final String saved = order.getDataId() > 0 ? Lang.TEXT_YES.getText(sender) : Lang.TEXT_NO.getText(sender);
                String buyer = order.getBuyer() != null ? PlayerProvider.getName(order.getBuyer()) : "<unknown>";
                if (buyer == null) {
                    buyer = order.getBuyer().toString();
                }
                boolean first = true;
                for (String s : Lang.COMMAND_DISPLAY_ORDER_INFO.getDisplay(sender)) {
                    s = Strings.replaceArgs(s, key, saved, order.getGroup(), buyer, order.getDate(), order.getExecution().name());
                    if (first) {
                        first = false;
                        sender.sendMessage(INDEX.replace("#", String.valueOf(orderNum)) + s);
                    } else {
                        sender.sendMessage("   " + s);
                    }
                }
                first = true;
                int cmdNum = 1;
                for (StoreOrder.Item item : order.getItems(currentGroup)) {
                    for (String s : Lang.COMMAND_DISPLAY_ORDER_ITEM_INFO.getDisplay(sender)) {
                        s = Strings.replaceArgs(s, item.getId(), item.getAmount(), item.getPrice(), PixelBuy.get().getLang().getLangText(sender, "Order." + order.getExecution() + "." + item.getState()));
                        if (first) {
                            first = false;
                            sender.sendMessage("   " + INDEX.replace("#", String.valueOf(cmdNum)) + s);
                        } else {
                            sender.sendMessage("      " + s);
                        }
                    }
                    first = true;
                    cmdNum++;
                }
                orderNum++;
            }
        });
    }

    public void calculate(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        getUserAsync(sender, cmd[cmd.length - 2], (user) -> {
            final float amount = PixelBuy.get().getStore().getCheckout().donated(user);
            sendLang(sender, "Calculate.Amount", user.getName() == null ? user.getUniqueId() : user.getName(), amount);
        });
    }
}
