package com.saicone.pixelbuy.managers.store;

import java.util.List;

public class StoreItem {

    private final String identifier;

    private final String price;

    private final boolean online;

    private List<String> actions;

    public StoreItem(String identifier, String price, boolean online, List<String> actions) {
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

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public void buy(String player, Integer orderID) {
        actions.forEach(string -> {
            ActionType action = StoreManager.parseAction(string, getPrice());
            if (action != null) action.executeBuy(player, orderID);
        });
    }

    public void refund(String player, Integer orderID) {
        actions.forEach(string -> {
            ActionType action = StoreManager.parseAction(string, getPrice());
            if (action != null && action.isRefundable()) action.executeRefund(player, orderID);
        });
    }
}
