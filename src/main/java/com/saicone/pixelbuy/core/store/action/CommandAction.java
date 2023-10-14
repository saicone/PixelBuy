package com.saicone.pixelbuy.core.store.action;

import com.saicone.pixelbuy.api.store.StoreAction;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class CommandAction extends StoreAction {

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
