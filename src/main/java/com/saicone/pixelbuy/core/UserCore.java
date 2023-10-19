package com.saicone.pixelbuy.core;

import com.saicone.pixelbuy.PixelBuy;

import com.saicone.pixelbuy.api.event.OrderProcessedEvent;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.core.store.StoreItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UserCore {

    private final PixelBuy plugin = PixelBuy.get();

    private final Map<UUID, StoreUser> players = new HashMap<>();

    public UserCore() {
        loadPlayers();
    }

    public void shut() {
        Bukkit.getOnlinePlayers().forEach(this::unloadPlayer);
        players.clear();
    }

    public void loadPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::loadPlayer);
    }

    public void loadPlayer(@NotNull Player player) {
        final StoreUser user = plugin.getDatabase().getData((PixelBuy.settings().getBoolean("Database.UUID") ? player.getUniqueId().toString() : player.getName()));
        if (user != null) {
            players.put(player.getUniqueId(), processData(player, user));
        }
    }

    public void unloadPlayer(@NotNull Player player) {
        plugin.getDatabase().saveData(players.get(player.getUniqueId()));
        players.remove(player.getUniqueId());
    }

    @SuppressWarnings("deprecation")
    public boolean processOrder(@NotNull String player, @NotNull StoreOrder order, boolean callEvent) {
        if (callEvent) {
            final OrderProcessedEvent event = new OrderProcessedEvent(player, order);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
        }
        final Player onlinePlayer = Bukkit.getPlayer(player);
        StoreUser user = getPlayerData(player);
        if (user != null) {
            if (isDuplicated(order.getId(), user.getOrders())) {
                return true;
            }
            user.addOrder(order);
        } else {
            final LinkedHashSet<StoreOrder> orders = new LinkedHashSet<>();
            orders.add(order);
            user = new StoreUser((PixelBuy.settings().getBoolean("Database.UUID") ? (onlinePlayer == null ? Bukkit.getOfflinePlayer(player).getUniqueId().toString() : onlinePlayer.getUniqueId().toString()) : player), 0.00, orders);
        }
        saveDataChanges(onlinePlayer, processData(onlinePlayer, user));
        return true;
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public StoreUser processData(@Nullable Player player, @NotNull StoreUser user) {
        float donated = Double.valueOf(user.getDonated()).floatValue();
        for (StoreOrder order : user.getOrders()) {
            for (StoreOrder.Item value : order.getItems()) {
                if (value.getState() != StoreOrder.State.PENDING) {
                    continue;
                }

                final StoreItem item = plugin.getStore().getItem(value.getId());
                if (item == null) {
                    value.state(StoreOrder.State.ERROR).error("The Store item '" + value.getId() + "' doesn't exist");
                    continue;
                }

                if (item.isOnline() && player == null) {
                    continue;
                }

                final StoreClient client = new StoreClient(player != null ? player : Bukkit.getOfflinePlayer(user.getPlayer()));
                client.parser(s -> s.replace("{order_player}", user.getPlayer()).replace("{order_id}", String.valueOf(order.getId())));
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                    try {
                        switch (value.getExecution()) {
                            case BUY:
                                item.onBuy(client);
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
                }, PixelBuy.settings().getLong("Order.Delay", 5L) * 20L);

                value.state(StoreOrder.State.DONE);

                if (value.getExecution() == StoreOrder.Execution.BUY) {
                    value.price(item.getPrice());
                    donated = donated + item.getPrice();
                }
            }
        }
        user.setDonated(donated);
        return user;
    }

    public boolean refundOrder(@NotNull String player, int id) {
        final StoreUser user = getPlayerData(player);
        if (user != null) {
            final Player onlinePlayer = Bukkit.getPlayer(player);
            for (StoreOrder order : user.getOrders()) {
                if (order.getId() == id) {
                    for (StoreOrder.Item item : order.getItems()) {
                        item.execution(StoreOrder.Execution.REFUND).state(StoreOrder.State.PENDING);
                    }
                    processData(onlinePlayer, user);
                    return true;
                }
            }
        }
        return false;
    }

    public void saveDataChanges(@Nullable Player player, @NotNull StoreUser user) {
        if (player != null) {
            players.remove(player.getUniqueId());
            players.put(player.getUniqueId(), user);
        } else {
            plugin.getDatabase().saveData(user);
        }
    }

    public void saveDataChanges(@NotNull String name, @NotNull StoreUser user) {
        final Player player = Bukkit.getPlayer(name);
        if (player != null) {
            players.put(player.getUniqueId(), user);
        }
        plugin.getDatabase().saveData(user);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public StoreUser getPlayerData(@NotNull String player) {
        final Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null) {
            return players.getOrDefault(onlinePlayer.getUniqueId(), null);
        } else {
            return plugin.getDatabase().getData(PixelBuy.settings().getBoolean("Database.UUID") ? Bukkit.getOfflinePlayer(player).getUniqueId().toString() : player);
        }
    }

    private boolean isDuplicated(int id, @NotNull Set<StoreOrder> list) {
        for (StoreOrder order : list) {
            if (order.getId() == id) {
                return true;
            }
        }
        return false;
    }
}
