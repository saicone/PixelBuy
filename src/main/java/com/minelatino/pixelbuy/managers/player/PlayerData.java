package com.minelatino.pixelbuy.managers.player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerData {

    private String player;
    private Integer donated;
    private List<Order> orders;

    public PlayerData(String player, Integer donated, List<Order> orders) {
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

    public Integer getDonated() {
        return donated;
    }

    public void setDonated(Integer donated) {
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
        private final Map<String, Byte> items;

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

        public void setItemState(String item, Byte state) {
            items.remove(item);
            items.put(item, state);
        }

        public boolean hasPending() {
            for (Byte state : items.values()) {
                if (state == 1) return true;
            }
            return false;
        }
    }
}
