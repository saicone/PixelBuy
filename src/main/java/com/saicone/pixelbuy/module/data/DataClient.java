package com.saicone.pixelbuy.module.data;

import com.saicone.pixelbuy.api.object.StoreUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DataClient {

    boolean setup();

    @NotNull
    String getType();

    void saveData(@NotNull StoreUser data);

    @Nullable
    StoreUser getData(@NotNull String player);

    @NotNull
    List<StoreUser> getAllData();

    void deleteData(@NotNull String player);
}
