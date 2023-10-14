package com.saicone.pixelbuy.core.store.action;

import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.util.MStrings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageAction extends StoreAction {

    @Override
    public @NotNull String getType() {
        return "MESSAGE";
    }

    @Override
    public boolean isRefundable() {
        return false;
    }

    @Override
    public void executeBuy(@NotNull String player, int orderID) {
        final Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null) {
            for (String msg : getExecutable(player, orderID).split("\\|")) {
                onlinePlayer.sendMessage(MStrings.color(msg));
            }
        }
    }
}
