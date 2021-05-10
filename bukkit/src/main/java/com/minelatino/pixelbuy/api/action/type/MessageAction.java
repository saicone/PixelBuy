package com.minelatino.pixelbuy.api.action.type;

import com.minelatino.pixelbuy.api.action.ActionType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class MessageAction extends ActionType<Player, OfflinePlayer> {

    @Override
    public String getName() {
        return "MESSAGE";
    }

    @Override
    public void executeOnline(Player player, Map<String, String> keys, String content) {

    }
}
