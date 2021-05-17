package com.minelatino.pixelbuy.api.object;

import com.minelatino.pixelbuy.PixelBuy;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerData {

    private final String uuid;
    private String name;
    private String displayName;
    private double donated;
    private final List<Order> orders;

    public PlayerData(String name) {
        this(PixelBuy.getPlugin().getPlayerUUID(name), name, name, 0, new ArrayList<>());
    }

    public PlayerData(String uuid, String name) {
        this(uuid, name, name, 0, new ArrayList<>());
    }

    public PlayerData(String uuid, String name, double donated, List<Order> orders) {
        this(uuid, name, name, donated, orders);
    }

    public PlayerData(UUID uuid, String name, String displayName, double donated, List<Order> orders) {
        this(uuid != null ? uuid.toString() : null, name, displayName, donated, orders);
    }

    public PlayerData(String uuid, String name, String displayName, double donated, List<Order> orders) {
        this.uuid = uuid;
        this.name = name;
        this.displayName = displayName;
        this.donated = donated;
        this.orders = orders;
    }

    @Nullable
    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getDonated() {
        return donated;
    }

    public void setDonated(double donated) {
        this.donated = donated;
    }

    public void addDonate(double sum) {
        donated = donated + sum;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<Order> getProcessedOrders() {
        return orders.stream().filter(Order::isProcessed).collect(Collectors.toList());
    }

    public List<Order> getUnprocessedOrders() {
        return orders.stream().filter(o -> !o.isProcessed()).collect(Collectors.toList());
    }

    public List<Order> getRefundedOrders() {
        return orders.stream().filter(Order::isRefunded).collect(Collectors.toList());
    }

    public void addOrders(List<Order> orders) {
        orders.forEach(this::addOrder);
    }

    public void addOrder(int id, List<String> items) {
        addOrder(new Order(id, items));
    }

    public void addOrder(Order order) {
        int num = 0;
        for (Order o : orders) {
            if (o.getId().equals(order.getId())) {
                // Debug (WARNING): Two orders with same ID!
                num++;
            }
        }
        if (num > 0) {
            order.addParam("DUPLICATED:" + num);
        }
        orders.add(order);
    }

    public void removeOrder(Integer id) {
        orders.removeIf(order -> order.getId().equals(id));
    }
}
