package com.saicone.pixelbuy.api.store;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class StoreUser {

    private final UUID uniqueId;
    // username
    private final String name;
    private double donated;
    private Set<StoreOrder> orders;

    private transient boolean edited;

    public StoreUser(@NotNull UUID uniqueId, @NotNull String name, double donated) {
        this(uniqueId, name, donated, new LinkedHashSet<>());
    }

    public StoreUser(@NotNull UUID uniqueId, @NotNull String name, double donated, @NotNull Set<StoreOrder> orders) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.donated = donated;
        this.orders = orders instanceof LinkedHashSet ? orders : new LinkedHashSet<>(orders);
    }

    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public float getDonated() {
        return 0.0f;
    }

    public double getDonatedOld() {
        return donated;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setDonated(double donated) {
        this.edited = true;
        this.donated = donated;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
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
