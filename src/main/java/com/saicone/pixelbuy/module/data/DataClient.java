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

    void getUser(@NotNull UUID uniqueId, @NotNull String username, @NotNull Consumer<StoreUser> consumer);

    void getUsers(@NotNull Consumer<StoreUser> consumer);

    void getOrders(@NotNull UUID buyer, @NotNull Consumer<StoreOrder> consumer);

    void saveUser(@NotNull StoreUser user);

    void saveUsers(@NotNull Collection<StoreUser> users);

    void saveOrders(@NotNull Collection<StoreOrder> orders);

}
