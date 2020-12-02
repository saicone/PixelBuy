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
    public void executeBuy(String player, Integer orderID) {
        Player p = Utils.getPlayer(player);
        if (p != null) {
            for (String msg : getExecutable(player, orderID).split("\\|")) {
                p.sendMessage(Utils.color(msg));
            }
        }
    }
}
