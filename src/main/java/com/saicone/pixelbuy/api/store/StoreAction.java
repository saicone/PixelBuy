package com.saicone.pixelbuy.api.store;

import org.jetbrains.annotations.NotNull;

public abstract class StoreAction {

    private String executable;
    private String price;

    @NotNull
    public abstract String getType();

    public abstract boolean isRefundable();

    public void executeBuy(@NotNull String player, int orderID) {
        // empty default method
    }

    public void executeRefund(@NotNull String player, int orderID) {
        // empty default method
    }

    public void setParts(@NotNull String executable, @NotNull String price) {
        this.executable = executable;
        this.price = price;
    }

    @NotNull
    public String getExecutable() {
        return executable;
    }

    @NotNull
    public String getExecutable(@NotNull String player, int orderID) {
        return executable.replace("%player%", player).replace("%orderID%", String.valueOf(orderID)).replace("%itemPrice%", price);
    }
}
