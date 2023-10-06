package com.saicone.pixelbuy.module.data;

import com.saicone.pixelbuy.api.object.StoreUser;

import java.util.List;

public interface DataClient {

    boolean setup();

    String getType();

    void saveData(StoreUser data);

    StoreUser getData(String player);

    List<StoreUser> getAllData();

    void deleteData(String player);
}
