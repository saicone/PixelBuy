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

    private final PixelStore store;

    private long executionDelay = -1;
    private boolean usersLoaded;
    private final Set<String> append = new HashSet<>();

    private boolean registered;

    public Checkout(@NotNull PixelStore store) {
        this.store = store;
    }

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
        append.clear();
        for (var entry : store.getItems().entrySet()) {
            if (!entry.getValue().getAppend().isEmpty()) {
                append.addAll(entry.getValue().getAppend());
            }
        }
        Bukkit.getOnlinePlayers().forEach(this::load);
        if (!registered) {
            registered = true;
            Bukkit.getPluginManager().registerEvents(this, PixelBuy.get());
            if (usersLoaded) {
                PixelBuy.get().getDatabase().loadUsers(true);
            }
        } else if (usersLoaded) {
            PixelBuy.get().getDatabase().loadUsers(false);
        }
    }

    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(this::unload);
    }

    @NotNull
    public PixelStore getStore() {
        return store;
    }

    public long getExecutionDelay() {
        return executionDelay;
    }

    public boolean isUsersLoaded() {
        return usersLoaded;
    }

    public void load(@NotNull Player player) {
        PixelBuy.get().getDatabase().loadUser(false, player.getUniqueId(), player.getName(), user -> {
            for (StoreOrder order : user.getOrders()) {
                append(order);
            }
            process(user);
        });
    }

    public void unload(@NotNull Player player) {
        final StoreUser user = PixelBuy.get().getDatabase().getCached(player.getUniqueId());
        if (user != null) {
            PixelBuy.get().getDatabase().saveDataAsync(user);
            if (usersLoaded) {
                user.setLoaded(false);
                user.getOrders().clear();
            } else {
                PixelBuy.get().getDatabase().getCached().remove(player.getUniqueId());
            }
        }
    }

    public void append(@NotNull StoreOrder order) {
        if (append.contains(order.getGroup())) {
            for (var entry : store.getItems().entrySet()) {
                if (entry.getValue().getAppend().contains(order.getGroup())) {
                    order.addItem(store.getGroup(), entry.getValue().getId());
                }
            }
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
            PixelBuy.get().getDatabase().loadOrders(true, user);
        }

        final StoreOrder o = user.mergeOrder(order);
        append(o);
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
        for (StoreOrder order : user.getOrders()) {
            boolean requireOnline = false;
            for (StoreOrder.Item value : order.getItems(store.getGroup())) {
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
                    Bukkit.getScheduler().runTaskLaterAsynchronously(PixelBuy.get(), () -> execute(client, order, item, value), executionDelay);
                } else {
                    execute(client, order, item, value);
                }

                value.state(StoreOrder.State.DONE);

                if (order.getExecution() == StoreOrder.Execution.BUY) {
                    value.price(item.getPrice());
                }
            }
        }
        // Calculate donated
        donated(user);
    }

    private void execute(@NotNull StoreClient client, @NotNull StoreOrder order, @NotNull StoreItem item, @NotNull StoreOrder.Item value) {
        try {
            switch (order.getExecution()) {
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
    }

    public float donated(@NotNull StoreUser user) {
        float donated = 0.0f;
        for (StoreOrder order : user.getOrders()) {
            if (order.getExecution() == StoreOrder.Execution.REFUND) {
                continue;
            }
            if (order.getGroup().equals(store.getGroup()) || !order.getItems().isEmpty()) {
                for (StoreOrder.Item item : order.getItems()) {
                    donated += item.getPrice();
                }
            } else if (!order.getAllItems().isEmpty()) {
                final Map<String, Float> map = new HashMap<>();
                for (var entry : order.getAllItems().entrySet()) {
                    for (StoreOrder.Item item : entry.getValue()) {
                        float current = map.getOrDefault(item.getId(), 0.0f);
                        if (item.getPrice() > current) {
                            map.put(item.getId(), item.getPrice());
                        }
                    }
                }
                for (Map.Entry<String, Float> entry : map.entrySet()) {
                    donated += entry.getValue();
                }
            }
        }
        if (user.getDonated() != donated) {
            // Update donated
            user.setDonated(donated);
        }
        return donated;
    }
}
