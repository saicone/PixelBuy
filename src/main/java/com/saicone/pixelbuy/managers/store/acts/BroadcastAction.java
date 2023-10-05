package com.saicone.pixelbuy.managers.store.acts;

import com.saicone.pixelbuy.managers.store.ActionType;
import com.saicone.pixelbuy.util.Utils;
import org.bukkit.Bukkit;

public class BroadcastAction extends ActionType {

    @Override
    public String getType() {
        return "BROADCAST";
    }

    @Override
    public boolean isRefundable() {
        return false;
    }

    @Override
    public void executeBuy(String player, Integer orderID) {
        for (String msg : getExecutable(player, orderID).split("\\|")) {
            broadcast(msg);
        }
    }

    private void broadcast(String s) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Utils.color(s)));
    }
}
