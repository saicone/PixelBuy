package com.minelatino.pixelbuy.managers.store;

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

    public String getPrice() {
        return price;
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
}
