package com.minelatino.pixelbuy.managers.store.acts;

import com.minelatino.pixelbuy.managers.store.ActionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
    public void executeBuy(Player player, Integer orderID) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), getExecutable(player.getName(), orderID).split("\\|")[0]);
    }

    @Override
    public void executeRefund(Player player, Integer orderID) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), getExecutable(player.getName(), orderID).split("\\|", 2)[1]);
    }
}
