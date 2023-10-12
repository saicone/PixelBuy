package com.saicone.pixelbuy.module.action.type;

import com.saicone.pixelbuy.module.action.ActionType;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class CommandAction extends ActionType {

    @Override
    public @NotNull String getType() {
        return "COMMAND";
    }

    @Override
    public boolean isRefundable() {
        return true;
    }

    @Override
    public void executeBuy(@NotNull String player, int orderID) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), getExecutable(player, orderID).split("\\|")[0]);
    }

    @Override
    public void executeRefund(@NotNull String player, int orderID) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), getExecutable(player, orderID).split("\\|", 2)[1]);
    }
}
