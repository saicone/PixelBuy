package com.minelatino.pixelbuy.managers.store.acts;

import com.minelatino.pixelbuy.managers.store.ActionType;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.entity.Player;

public class MessageAction extends ActionType {

    @Override
    public String getType() {
        return "MESSAGE";
    }

    @Override
    public boolean isRefundable() {
        return false;
    }

    @Override
    public void executeBuy(Player player, Integer orderID) {
        for (String msg : getExecutable(player.getName(), orderID).split("\\|")) {
            player.sendMessage(Utils.color(msg));
        }
    }
}
