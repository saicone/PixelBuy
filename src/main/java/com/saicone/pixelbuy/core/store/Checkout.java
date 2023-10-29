package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.PixelBuy;

import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Checkout implements Listener {

    private static final Set<String> PLACEHOLDER_TYPE = Set.of("user", "order", "store");

    private long executionDelay = -1;
    private boolean usersLoaded;

    private boolean registered;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        unload(event.getPlayer());
    }

    public void onLoad() {
        executionDelay = PixelBuy.settings().getRegex("(?i)order|user-?data", "(?i)execution-?delay").asLong(100L);
        usersLoaded = PixelBuy.settings().getRegex("(?i)order|user-?data", "(?i)load-?users").asBoolean(true);
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, PixelBuy.get());
        }
        Bukkit.getOnlinePlayers().forEach(this::load);
    }

    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(this::unload);
    }

    public long getExecutionDelay() {
        return executionDelay;
    }

    public boolean isUsersLoaded() {
        return usersLoaded;
    }

    public void load(@NotNull Player player) {
        PixelBuy.get().getDatabase().getDataAsync(player.getUniqueId(), player.getName());
    }

    public void unload(@NotNull Player player) {
        final StoreUser user = PixelBuy.get().getDatabase().getCached(player.getUniqueId());
        if (user != null) {
            PixelBuy.get().getDatabase().saveDataAsync(user);
            user.setLoaded(false);
            user.getOrders().clear();
        }
    }

    public boolean process(@NotNull StoreOrder order) {
        if (order.getBuyer() == null) {
            return false;
        }

        StoreUser user = PixelBuy.get().getDatabase().getCached(order.getBuyer());
        final OfflinePlayer player = Bukkit.getOfflinePlayer(order.getBuyer());
        if (user == null) {
            if (player.getName() == null) {
                return false;
            }
            user = PixelBuy.get().getDatabase().getData(player.getUniqueId(), player.getName());
        } else if (!user.isLoaded()) {
            PixelBuy.get().getDatabase().loadOrders(user);
        }

        user.mergeOrder(order);
        user.setEdited(true);
        process(user);
        return true;
    }

    public void process(@NotNull UUID uniqueId) {
        final StoreUser user = PixelBuy.get().getDatabase().getCached(uniqueId);
        if (user != null) {
            process(user);
        }
    }

    public void process(@NotNull StoreUser user) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(user.getUniqueId());
        final PixelStore store = PixelBuy.get().getStore();
        for (StoreOrder order : user.getOrders()) {
            boolean requireOnline = false;
            for (StoreOrder.Item value : order.getItems()) {
                if (value.getState() != StoreOrder.State.PENDING) {
                    continue;
                }

                final StoreItem item = store.getItem(value.getId());
                if (item == null) {
                    value.state(StoreOrder.State.ERROR).error("The Store item '" + value.getId() + "' doesn't exist");
                    continue;
                }

                if (item.isOnline() && !player.isOnline()) {
                    requireOnline = true;
                    continue;
                }

                if (requireOnline && !item.isAlwaysRun()) {
                    continue;
                }

                final StoreClient client = new StoreClient(player);
                client.parser(s -> Strings.replaceBracketPlaceholder(s, PLACEHOLDER_TYPE::contains, (id, arg) -> {
                    final String field = arg.toLowerCase();
                    final Object finalValue;
                    switch (id.toLowerCase()) {
                        case "user":
                            finalValue = user.get(field);
                            break;
                        case "order":
                            if (field.startsWith("item_")) {
                                finalValue = value.get(field.substring(5));
                            } else {
                                finalValue = order.get(field);
                            }
                            break;
                        case "store":
                            if (field.startsWith("item_")) {
                                finalValue = item.get(field.substring(5));
                            } else {
                                finalValue = store.get(field);
                            }
                            break;
                        default:
                            finalValue = null;
                            break;
                    }
                    return finalValue != null ? finalValue : "{" + id + "_" + arg + "}";
                }));

                if (executionDelay > 0) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(PixelBuy.get(), () -> execute(user, client, order, item, value), executionDelay);
                } else {
                    execute(user, client, order, item, value);
                }

                value.state(StoreOrder.State.DONE);

                if (order.getExecution() == StoreOrder.Execution.BUY) {
                    value.price(item.getPrice());
                }
            }
        }
    }

    private void execute(@NotNull StoreUser user, @NotNull StoreClient client, @NotNull StoreOrder order, @NotNull StoreItem item, @NotNull StoreOrder.Item value) {
        try {
            switch (order.getExecution()) {
                case BUY:
                    item.onBuy(client);
                    user.addDonated(item.getPrice());
                    break;
                case RECOVER:
                    item.onRecover(client);
                    break;
                case REFUND:
                    item.onRefund(client);
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            value.state(StoreOrder.State.ERROR).error(t.getClass().getName() + " | " + t.getMessage());
        }
    }

}
