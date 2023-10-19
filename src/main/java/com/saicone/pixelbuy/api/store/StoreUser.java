package com.saicone.pixelbuy.api.store;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StoreUser {

    private String player;
    private double donated;
    private Set<StoreOrder> orders;

    public StoreUser(@NotNull String player, double donated, @NotNull Set<StoreOrder> orders) {
        this.player = player;
        this.donated = donated;
        this.orders = orders instanceof LinkedHashSet ? orders : new LinkedHashSet<>(orders);
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
    public StoreOrder getOrder(int id) {
        for (final StoreOrder order : orders) {
            if (order.getId() == id) {
                return order;
            }
        }
        return null;
    }

    @NotNull
    public Set<StoreOrder> getOrders() {
        return orders;
    }

    @NotNull
    public Set<StoreOrder> getOrders(@NotNull StoreOrder.State state) {
        return orders.stream().filter(order -> order.has(state)).collect(Collectors.toSet());
    }

    public void setOrders(@NotNull Set<StoreOrder> orders) {
        this.orders = orders instanceof LinkedHashSet ? orders : new LinkedHashSet<>(orders);
    }

    public void addOrders(@NotNull Collection<StoreOrder> orders) {
        this.orders.addAll(orders);
    }

    public void addOrder(@NotNull StoreOrder order) {
        orders.add(order);
    }

    public void updateOrder(@NotNull StoreOrder order) {
        removeOrder(order.getId());
        orders.add(order);
    }

    public void removeOrder(int id) {
        orders.removeIf(order -> order.getId() == id);
    }

}
