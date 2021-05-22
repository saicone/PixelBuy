package com.minelatino.pixelbuy.module.data;

import com.minelatino.pixelbuy.api.object.PlayerData;
import com.minelatino.pixelbuy.api.object.ServerData;

public interface DataType {

    boolean setup(String identifier);

    void savePlayerData(PlayerData data);

    PlayerData getPlayerData(String player);

    void saveServerData(ServerData data);

    ServerData getServerData(String server);
}
