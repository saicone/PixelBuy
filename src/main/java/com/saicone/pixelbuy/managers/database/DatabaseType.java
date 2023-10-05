package com.saicone.pixelbuy.managers.database;

import com.saicone.pixelbuy.managers.player.PlayerData;

import java.util.List;

public interface DatabaseType {

    boolean setup();

    String getType();

    void saveData(PlayerData data);

    PlayerData getData(String player);

    List<PlayerData> getAllData();

    void deleteData(String player);
}
