package com.saicone.pixelbuy.module.data;

import com.saicone.pixelbuy.api.object.PlayerData;

import java.util.List;

public interface DatabaseType {

    boolean setup();

    String getType();

    void saveData(PlayerData data);

    PlayerData getData(String player);

    List<PlayerData> getAllData();

    void deleteData(String player);
}
