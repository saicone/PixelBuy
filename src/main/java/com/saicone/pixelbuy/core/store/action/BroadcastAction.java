package com.saicone.pixelbuy.core.store.action;

import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.util.MStrings;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class BroadcastAction extends StoreAction {

    @Override
    public @NotNull String getType() {
        return "BROADCAST";
    }

    @Override
    public boolean isRefundable() {
        return false;
    }

    @Override
    public void executeBuy(@NotNull String player, int orderID) {
        for (String msg : getExecutable(player, orderID).split("\\|")) {
            broadcast(msg);
        }
    }

    private void broadcast(@NotNull String msg) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(MStrings.color(msg)));
    }
}
