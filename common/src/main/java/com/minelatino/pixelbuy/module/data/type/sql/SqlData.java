package com.minelatino.pixelbuy.module.data.type.sql;

import com.minelatino.pixelbuy.api.object.PlayerData;
import com.minelatino.pixelbuy.api.object.ServerData;
import com.minelatino.pixelbuy.module.config.PathSection;
import com.minelatino.pixelbuy.module.data.DataType;
import com.minelatino.pixelbuy.module.data.Database;

import java.util.HashMap;
import java.util.Map;

public class SqlData implements DataType {
    
    private final Map<String, String> querys = new HashMap<>();

    @Override
    public boolean setup(String identifier) {
        PathSection config = Database.getConfig().getSection(identifier + ".sql");
        querys.put("player-table", config.getString("query.player-table", "CREATE TABLE IF NOT EXISTS '%prefix%PlayerData' (player {0} NOT NULL, donated {1}, data {2}, PRIMARY KEY(player));"));
        querys.put("server-table", config.getString("query.player-table", "CREATE TABLE IF NOT EXISTS '%prefix%ServerData' (server {0} NOT NULL, data {1}, PRIMARY KEY(server));"));
        querys.put("update-column", config.getString("query.update-column", "ALTER TABLE %prefix%{0} MODIFY {1} {2};"));
        querys.put("save-player", config.getString("query.save-player", "INSERT INTO '%prefix%PlayerData' (player, donated, data) VALUES ('{0}', '{1}', '{2}') ON DUPLICATE KEY UPDATE 'donated' = '{1}', 'data' = '{2}';"));
        querys.put("get-player", config.getString("query.get-player", "SELECT 'data' FROM '%prefix%PlayerData' WHERE 'player' = '{0}';"));
        querys.put("save-server", config.getString("query.save-server", "INSERT INTO '%prefix%ServerData' (server, data) VALUES ('{0}', '{1}') ON DUPLICATE KEY UPDATE 'data' = '{1}';"));
        querys.put("get-server", config.getString("query.get-server", "SELECT 'data' FROM '%prefix%ServerData' WHERE 'server' = '{0}';"));
        return false;
    }

    @Override
    public void savePlayerData(PlayerData data) {

    }

    @Override
    public PlayerData getPlayerData(String player) {
        return null;
    }

    @Override
    public void saveServerData(ServerData data) {

    }

    @Override
    public ServerData getServerData(String server) {
        return null;
    }
}
