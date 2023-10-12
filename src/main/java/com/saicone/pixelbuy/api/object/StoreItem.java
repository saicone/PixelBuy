package com.saicone.pixelbuy.api.object;

import com.saicone.pixelbuy.core.PixelStore;
import com.saicone.pixelbuy.module.action.ActionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StoreItem {

    private final String identifier;

    private final String price;

    private final boolean online;

    private List<String> actions;

    public StoreItem(@NotNull String identifier, @NotNull String price, boolean online, @NotNull List<String> actions) {
        this.identifier = identifier;
        this.price = price;
        this.online = online;
        this.actions = actions;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @NotNull
    public String getOriginalPrice() {
        return price.split("->", 2)[0];
    }

    @NotNull
    public String getPrice() {
        String[] prices = this.price.split("->", 2);
        return (prices.length > 1 ? prices[1] : price);
    }

    public boolean isOnline() {
        return online;
    }

    @NotNull
    public List<String> getActions() {
        return actions;
    }

    public void setActions(@NotNull List<String> actions) {
        this.actions = actions;
    }

    public void buy(@NotNull String player, int orderID) {
        actions.forEach(string -> {
            final ActionType action = PixelStore.parseAction(string, getPrice());
            if (action != null) {
                action.executeBuy(player, orderID);
            }
        });
    }

    public void refund(@NotNull String player, int orderID) {
        actions.forEach(string -> {
            final ActionType action = PixelStore.parseAction(string, getPrice());
            if (action != null && action.isRefundable()) {
                action.executeRefund(player, orderID);
            }
        });
    }
}
