package com.minelatino.pixelbuy.managers.store;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class StoreItem {

    private final String identifier;

    private final String price;

    private final boolean online;

    private List<ActionType> actions;

    public StoreItem(String identifier, String price, boolean online, List<ActionType> actions) {
        this.identifier = identifier;
        this.price = price;
        this.online = online;
        this.actions = actions;
    }

    public String getIdentifier() {
        return identifier;
    }


    public String getOriginalPrice() {
        return price.split("->", 2)[0];
    }

    public String getPrice() {
        String[] prices = this.price.split("->", 2);
        return (prices.length > 1 ? prices[1] : price);
    }

    public boolean isOnline() {
        return online;
    }

    public List<ActionType> getActions() {
        return actions;
    }

    public void setActions(List<ActionType> actions) {
        this.actions = actions;
    }

    public void refund(String player, Integer orderID) {
        actions.forEach(action -> {
            if (action.isRefundable()) action.executeRefund(player, orderID);
        });
    }
}
