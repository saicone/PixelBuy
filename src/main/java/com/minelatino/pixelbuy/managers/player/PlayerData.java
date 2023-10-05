package com.minelatino.pixelbuy.managers.player;

import java.util.HashMap;
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
        orders.removeIf(order -> order.getId().equals(id));
    }

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

        public Map<String, Byte> getItems(byte type) {
            Map<String, Byte> items = new HashMap<>();
            for (Map.Entry<String, Byte> item : this.items.entrySet()) {
                if (item.getValue() >= type) items.put(item.getKey(), item.getValue());
            }
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

        public boolean pending(boolean pending) {
            if (pending) {
                return items.containsValue((byte) 1);
            } else {
                return !items.containsValue((byte) 1);
            }
        }
    }
}
