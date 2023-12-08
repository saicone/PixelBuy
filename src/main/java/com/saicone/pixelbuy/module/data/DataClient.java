package com.saicone.pixelbuy.module.data;

import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public interface DataClient {

    void onLoad(@NotNull BukkitSettings config);

    default void onStart() {
        // empty default method
    }

    default void onClose() {
        // empty default method
    }

    default void getUserAsync(@NotNull UUID uniqueId, @NotNull String username, @NotNull Consumer<StoreUser> consumer) {
        getUser(false, uniqueId, username, consumer);
    }

    default void getUser(@NotNull UUID uniqueId, @NotNull String username, @NotNull Consumer<StoreUser> consumer) {
        getUser(true, uniqueId, username, consumer);
    }

    void getUser(boolean sync, @NotNull UUID uniqueId, @NotNull String username, @NotNull Consumer<StoreUser> consumer);

    default void getUsersAsync(@NotNull Consumer<StoreUser> consumer) {
        getUsers(false, consumer);
    }

    default void getUsers(@NotNull Consumer<StoreUser> consumer) {
        getUsers(true, consumer);
    }

    void getUsers(boolean sync, @NotNull Consumer<StoreUser> consumer);

    default void getOrderAsync(@NotNull String provider, int id, @NotNull String group, @NotNull Consumer<StoreOrder> consumer) {
        getOrder(false, provider, id, group, consumer);
    }

    default void getOrder(@NotNull String provider, int id, @NotNull String group, @NotNull Consumer<StoreOrder> consumer) {
        getOrder(true, provider, id, group, consumer);
    }

    void getOrder(boolean sync, @NotNull String provider, int id, @NotNull String group, @NotNull Consumer<StoreOrder> consumer);

    default void getOrdersAsync(@NotNull UUID buyer, @NotNull Consumer<StoreOrder> consumer) {
        getOrders(false, buyer, consumer);
    }

    default void getOrders(@NotNull UUID buyer, @NotNull Consumer<StoreOrder> consumer) {
        getOrders(true, buyer, consumer);
    }

    void getOrders(boolean sync, @NotNull UUID buyer, @NotNull Consumer<StoreOrder> consumer);

    default void saveUserAsync(@NotNull StoreUser user) {
        saveUser(false, user);
    }

    default void saveUser(@NotNull StoreUser user) {
        saveUser(true, user);
    }

    void saveUser(boolean sync, @NotNull StoreUser user);

    default void saveUsersAsync(@NotNull Collection<StoreUser> users) {
        saveUsers(false, users);
    }

    default void saveUsers(@NotNull Collection<StoreUser> users) {
        saveUsers(true, users);
    }

    void saveUsers(boolean sync, @NotNull Collection<StoreUser> users);

    default void saveOrderAsync(@NotNull StoreOrder order) {
        saveOrder(false, order);
    }

    default void saveOrder(@NotNull StoreOrder order) {
        saveOrder(true, order);
    }

    void saveOrder(boolean sync, @NotNull StoreOrder order);

    default void saveOrdersAsync(@NotNull Collection<StoreOrder> orders) {
        saveOrders(false, orders);
    }

    default void saveOrders(@NotNull Collection<StoreOrder> orders) {
        saveOrders(true, orders);
    }

    void saveOrders(boolean sync, @NotNull Collection<StoreOrder> orders);

    default void deleteOrderAsync(@NotNull String provider, int id) {
        deleteOrder(false, provider, id);
    }

    default void deleteOrder(@NotNull String provider, int id) {
        deleteOrder(true, provider, id);
    }

    void deleteOrder(boolean sync, @NotNull String provider, int id);

}
