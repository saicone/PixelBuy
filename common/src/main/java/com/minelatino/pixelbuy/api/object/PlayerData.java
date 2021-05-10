package com.minelatino.pixelbuy.api.object;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerData {

    private String uuid;
    private String name;
    private String displayName;
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

    public List<Order> getOrders(byte type) {
        return orders.stream().filter(order -> order.hasType(type)).collect(Collectors.toList());
    }

}
