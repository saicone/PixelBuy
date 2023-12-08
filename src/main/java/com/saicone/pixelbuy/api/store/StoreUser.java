package com.saicone.pixelbuy.api.store;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class StoreUser {

    private final UUID uniqueId;
    // username
    private final String name;
    private float donated;
    private Set<StoreOrder> orders;

    private transient boolean loaded;
    private transient boolean edited;

    public StoreUser(@NotNull UUID uniqueId, @NotNull String name, float donated) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.donated = donated;
        this.orders = new LinkedHashSet<>();
    }

    @Nullable
    public Object get(@NotNull String field) {
        switch (field) {
            case "uuid":
            case "uniqueid":
            case "unique_id":
                return uniqueId;
            case "id":
                return uniqueId.toString().replace('-', '\0');
            case "name":
                return name;
            case "donated":
                return donated;
            case "orders":
                return orders.size();
            default:
                return null;
        }
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
        return donated;
    }

    @Nullable
    public StoreOrder getOrder(int id) {
        return getOrder(order -> order.getId() == id);
    }

    @Nullable
    public StoreOrder getOrder(@NotNull Predicate<StoreOrder> filter) {
        return orders.stream().filter(filter).findFirst().orElse(null);
    }

    @NotNull
    public Set<StoreOrder> getOrders() {
        return orders;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setDonated(float donated) {
        this.edited = true;
        this.donated = donated;
    }

    public void setOrders(@NotNull Set<StoreOrder> orders) {
        this.orders = orders instanceof LinkedHashSet ? orders : new LinkedHashSet<>(orders);
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public void addDonated(float donated) {
        this.donated = this.donated + donated;
    }

    public boolean addOrders(@NotNull Collection<StoreOrder> orders) {
        return this.orders.addAll(orders);
    }

    public boolean addOrder(@NotNull StoreOrder order) {
        if (order.getBuyer() == null || !uniqueId.equals(order.getBuyer())) {
            order.setBuyer(uniqueId);
        }
        return orders.add(order);
    }

    @NotNull
    public StoreOrder mergeOrder(@NotNull StoreOrder order) {
        if (order.getBuyer() == null || !uniqueId.equals(order.getBuyer())) {
            order.setBuyer(uniqueId);
        }
        if (orders.add(order)) {
            return order;
        }
        // Dirty solution, may produce a bad performance if there's a high amount of orders
        final Iterator<StoreOrder> iterator = orders.iterator();
        while (iterator.hasNext()) {
            final StoreOrder o = iterator.next();
            if (o.equals(order)) {
                o.merge(order);
                return o;
            }
        }
        throw new IllegalArgumentException("The order cannot be merged");
    }

    public void updateOrder(@NotNull StoreOrder order) {
        removeOrder(order);
        orders.add(order);
    }

    public void removeOrder(@NotNull StoreOrder order) {
        orders.remove(order);
    }

    public void removeOrder(@NotNull String provider, int id, @NotNull String group) {
        final Iterator<StoreOrder> iterator = orders.iterator();
        while (iterator.hasNext()) {
            final StoreOrder order = iterator.next();
            if (order.getProvider().equals(provider) && order.getId() == id && order.getGroup().equals(group)) {
                iterator.remove();
                return;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof UUID) return uniqueId.equals(o);
        if (o == null || getClass() != o.getClass()) return false;

        StoreUser storeUser = (StoreUser) o;

        return uniqueId.equals(storeUser.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
}
