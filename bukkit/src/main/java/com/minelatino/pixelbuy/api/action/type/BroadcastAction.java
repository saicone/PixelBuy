package com.minelatino.pixelbuy.api.action.type;

import com.minelatino.pixelbuy.api.action.ActionType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class BroadcastAction extends ActionType<Player, OfflinePlayer> {

    @Override
    public String getName() {
        return "BROADCAST";
    }

    @Override
    public void execute(Map<String, String> keys, String content) {

    }
}
