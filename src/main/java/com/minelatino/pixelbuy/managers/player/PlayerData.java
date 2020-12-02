package com.minelatino.pixelbuy.managers.player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerData {

    private String player;
    private double donated;
    private List<Order> orders;

    public PlayerData(String player, double donated, List<Order> orders) {
        this.player = player;
        this.donated = donated;
        this.orders = orders;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public double getDonated() {
        return donated;
    }

    public void setDonated(double donated) {
        this.donated = donated;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void addOrders(List<Order> orders) {
        this.orders.addAll(orders);
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void updateOrder(Order order) {
        removeOrder(order.getId());
        orders.add(order);
    }

    public void removeOrder(Integer id) {
        orders.forEach(order -> {
            if (order.getId().equals(id)) orders.remove(order);
        });
    }

    public List<Order> getPendingOrders() {
        return orders.stream().filter(Order::hasPending).collect(Collectors.toList());
    }

    public static class Order {

        private final Integer id;
        // Item states:
        // 1 = Pending
        // 2 = Processed
        // 3 = Refunded
        private Map<String, Byte> items;

        public Order(Integer id, Map<String, Byte> items) {
            this.id = id;
            this.items = items;
        }

        public Integer getId() {
            return id;
        }

        public Map<String, Byte> getItems() {
            return items;
        }

        public void setItems(Map<String, Byte> items) {
            this.items = items;
        }

        public void setItemState(String item, Byte state) {
            items.remove(item);
            items.put(item, state);
        }

        public void removeItem(String item) {
            items.remove(item);
        }

        public boolean hasPending() {
            for (Byte state : items.values()) {
                if (state == 1) return true;
            }
            return false;
        }
    }
}
