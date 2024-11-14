package com.saicone.pixelbuy.module.data;

import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    StoreUser getUser(@NotNull UUID uniqueId, @NotNull String username);

    void getUsers(@NotNull Consumer<StoreUser> consumer);

    @Nullable
    StoreOrder getOrder(@NotNull String provider, int id, @NotNull String group);

    void getOrders(@NotNull UUID buyer, @NotNull Consumer<StoreOrder> consumer);

    void saveUser(@NotNull StoreUser user);

    void saveUsers(@NotNull Collection<StoreUser> users);

    void saveOrder(@NotNull StoreOrder order);

    void saveOrders(@NotNull Collection<StoreOrder> orders);

    void deleteOrder(@NotNull String provider, int id);

}
