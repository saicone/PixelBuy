package com.saicone.pixelbuy.module.action.type;

import com.saicone.pixelbuy.module.action.ActionType;
import com.saicone.pixelbuy.util.MStrings;
import org.bukkit.Bukkit;
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
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            for (String msg : getExecutable(player, orderID).split("\\|")) {
                p.sendMessage(MStrings.color(msg));
            }
        }
    }
}
