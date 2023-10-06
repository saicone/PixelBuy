package com.saicone.pixelbuy.module.action.type;

import com.saicone.pixelbuy.module.action.ActionType;
import org.bukkit.Bukkit;

public class CommandAction extends ActionType {

    @Override
    public String getType() {
        return "COMMAND";
    }

    @Override
    public boolean isRefundable() {
        return true;
    }

    @Override
    public void executeBuy(String player, Integer orderID) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), getExecutable(player, orderID).split("\\|")[0]);
    }

    @Override
    public void executeRefund(String player, Integer orderID) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), getExecutable(player, orderID).split("\\|", 2)[1]);
    }
}
