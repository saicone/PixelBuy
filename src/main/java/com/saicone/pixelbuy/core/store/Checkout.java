package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.PixelBuy;

import com.saicone.pixelbuy.api.event.OrderProcessEvent;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class Checkout {

    private static final Set<String> PLACEHOLDER_TYPE = Set.of("user", "order", "store");

    private final PixelStore store;

    private long executionDelay = -1;
    private final Set<String> append = new HashSet<>();

    public Checkout(@NotNull PixelStore store) {
        this.store = store;
    }

    public void onJoin(@NotNull StoreUser user) {
        for (StoreOrder order : user.getOrders()) {
            append(order);
        }
        process(user);
    }

    public void onLoad() {
        executionDelay = store.getConfig().getRegex("(?i)checkout", "(?i)execution-?delay").asLong(100L);
        append.clear();
        for (var entry : store.getItems().entrySet()) {
            if (!entry.getValue().getAppend().isEmpty()) {
                append.addAll(entry.getValue().getAppend());
            }
        }
    }

    @NotNull
    public PixelStore getStore() {
        return store;
    }

    public long getExecutionDelay() {
        return executionDelay;
    }

    public void append(@NotNull StoreOrder order) {
        if (!order.getGroup().equals(store.getGroup()) && append.contains(order.getGroup())) {
            for (var entry : store.getItems().entrySet()) {
                if (entry.getValue().getAppend().contains(order.getGroup())) {
                    if (order.getItems().contains(entry.getKey())) {
                        order.addItem(store.getGroup(), entry.getValue().getId());
                    }
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
        }
        if (!user.isLoaded()) {
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
        process(user, u -> {
            // Calculate donated
            final float donated = u.getDonated();
            if (donated != donated(u)) {
                PixelBuy.get().getDatabase().saveData(u);
            }
            if (Bukkit.getPlayer(user.getUniqueId()) != null) {
                PixelBuy.get().getDatabase().unloadUser(u);
            }
        });
    }

    public void process(@NotNull StoreUser user, @Nullable Consumer<StoreUser> consumer) {
        if (executionDelay > 0) {
            Bukkit.getScheduler().runTaskLater(PixelBuy.get(), () -> execute(user, consumer), executionDelay);
        } else if (Bukkit.isPrimaryThread()) {
            execute(user, consumer);
        } else {
            Bukkit.getScheduler().runTask(PixelBuy.get(), () -> execute(user, consumer));
        }
    }

    private void execute(@NotNull StoreUser user, @Nullable Consumer<StoreUser> consumer) {
        if (!user.isLoaded()) {
            return;
        }
        final List<StoreOrder> orders = new ArrayList<>();
        for (StoreOrder order : user.getOrders()) {
            if (!order.has(store.getGroup(), StoreOrder.State.PENDING)) {
                continue;
            }

            final OrderProcessEvent event = new OrderProcessEvent(user, order);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                continue;
            }

            orders.add(event.getOrder());
        }

        if (!orders.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> {
                if (!user.isLoaded()) {
                    return;
                }
                final OfflinePlayer player = Bukkit.getOfflinePlayer(user.getUniqueId());
                for (StoreOrder order : orders) {
                    execute(player, user, order);
                }
                if (consumer != null) {
                    consumer.accept(user);
                }
            });
        }
    }

    private void execute(@NotNull OfflinePlayer player, @NotNull StoreUser user, @NotNull StoreOrder order) {
        boolean requireOnline = false;
        for (StoreOrder.Item value : order.getItems(store.getGroup())) {
            if (value.getState() != StoreOrder.State.PENDING) {
                continue;
            }

            final StoreItem item = store.getItem(value.getId());
            if (item == null) {
                value.state(StoreOrder.State.ERROR).error("The store item '" + value.getId() + "' doesn't exist");
                order.setEdited(true);
                continue;
            }

            if (item.isOnline() && !player.isOnline()) {
                requireOnline = true;
                continue;
            }

            if (requireOnline && !item.isAlwaysRun()) {
                continue;
            }

            order.setEdited(true);

            value.state(StoreOrder.State.DONE);
            if (order.getExecution() == StoreOrder.Execution.BUY) {
                value.price(item.getPrice());
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

            execute(client, order, item, value);
        }

        if (order.isEdited()) {
            PixelBuy.get().getDatabase().saveData(order);
        }
    }

    private void execute(@NotNull StoreClient client, @NotNull StoreOrder order, @NotNull StoreItem item, @NotNull StoreOrder.Item value) {
        try {
            switch (order.getExecution()) {
                case BUY:
                    item.onBuy(client, value.getAmount());
                    break;
                case RECOVER:
                    item.onRecover(client, value.getAmount());
                    break;
                case REFUND:
                    item.onRefund(client, value.getAmount());
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            value.state(StoreOrder.State.ERROR).error(t.getClass().getName() + "\n" + t.getMessage());
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
