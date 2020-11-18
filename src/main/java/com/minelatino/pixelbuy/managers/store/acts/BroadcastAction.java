package com.minelatino.pixelbuy.managers.store.acts;

import com.minelatino.pixelbuy.managers.store.ActionType;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
    public void executeBuy(Player player, Integer orderID) {
        for (String msg : getExecutable(player.getName(), orderID).split("\\|")) {
            broadcast(msg);
        }
    }

    private void broadcast(String s) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Utils.color(s)));
    }
}
