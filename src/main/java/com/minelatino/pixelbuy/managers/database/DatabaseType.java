package com.minelatino.pixelbuy.managers.database;

import com.minelatino.pixelbuy.managers.player.PlayerData;

public interface DatabaseType {

    boolean setup();

    String getType();

    void saveData(PlayerData data);

    PlayerData getData(String player);

    void deleteData(String player);
}
