package com.saicone.pixelbuy.api.store;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class StoreUser {

    private final UUID uuid;
    // username
    private final String name;
    private double donated;
    private Set<StoreOrder> orders;

    public StoreUser(@NotNull UUID uuid, @NotNull String name, double donated, @NotNull Set<StoreOrder> orders) {
        this.uuid = uuid;
        this.name = name;
        this.donated = donated;
        this.orders = orders instanceof LinkedHashSet ? orders : new LinkedHashSet<>(orders);
    }

    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    @NotNull
    public String getName() {
        return name;
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
