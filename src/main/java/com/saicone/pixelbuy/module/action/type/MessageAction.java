package com.saicone.pixelbuy.module.action.type;

import com.saicone.pixelbuy.module.action.ActionType;
import com.saicone.pixelbuy.util.MStrings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageAction extends ActionType {

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
