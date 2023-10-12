package com.saicone.pixelbuy.api.object;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StoreUser {

    private String player;
    private double donated;
    private List<Order> orders;

    public StoreUser(@NotNull String player, double donated, @NotNull List<Order> orders) {
        this.player = player;
        this.donated = donated;
        this.orders = orders;
    }

    @NotNull
    public String getPlayer() {
        return player;
    }

    public void setPlayer(@NotNull String player) {
        this.player = player;
    }

    public double getDonated() {
        return donated;
    }

    public void setDonated(double donated) {
        this.donated = donated;
    }

    @Nullable
    public Order getOrder(int id) {
        for (final Order order : orders) {
            if (order.getId() == id) {
                return order;
            }
        }
        return null;
    }

    @NotNull
    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(@NotNull List<Order> orders) {
        this.orders = orders;
    }

    public void addOrders(@NotNull List<Order> orders) {
        this.orders.addAll(orders);
    }

    public void addOrder(@NotNull Order order) {
        orders.add(order);
    }

    public void updateOrder(@NotNull Order order) {
        removeOrder(order.getId());
        orders.add(order);
    }

    public void removeOrder(int id) {
        orders.removeIf(order -> order.getId() == id);
    }

    @NotNull
    public List<Order> getOrders(boolean pending) {
        return orders.stream().filter(order -> order.pending(pending)).collect(Collectors.toList());
    }

    public static class Order {

        private final Integer id;
        // Item states:
        // 1 = Pending
        // 2 = Processed
        // 3 = Refunded
        private Map<String, Byte> items;

        public Order(int id, @NotNull Map<String, Byte> items) {
            this.id = id;
            this.items = items;
        }

        public int getId() {
            return id;
        }

        @NotNull
        public Map<String, Byte> getItems() {
            return items;
        }

        @NotNull
        public Map<String, Byte> getItems(byte type) {
            final Map<String, Byte> items = new HashMap<>();
            for (Map.Entry<String, Byte> item : this.items.entrySet()) {
                if (item.getValue() >= type) items.put(item.getKey(), item.getValue());
            }
            return items;
        }

        public void setItems(@NotNull Map<String, Byte> items) {
            this.items = items;
        }

        public void setItemState(@NotNull String item, byte state) {
            items.remove(item);
            items.put(item, state);
        }

        public void removeItem(@NotNull String item) {
            items.remove(item);
        }

        public boolean pending(boolean pending) {
            if (pending) {
                return items.containsValue((byte) 1);
            } else {
                return !items.containsValue((byte) 1);
            }
        }
    }
}
