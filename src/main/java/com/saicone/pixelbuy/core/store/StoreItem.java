package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.api.store.StoreClient;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StoreItem {

    private final String identifier;

    private final String price;

    private final boolean online;

    private final List<StoreAction> onBuy;
    private final List<StoreAction> onRefund;

    public StoreItem(@NotNull String identifier, @NotNull String price, boolean online, @NotNull List<StoreAction> onBuy, @NotNull List<StoreAction> onRefund) {
        this.identifier = identifier;
        this.price = price;
        this.online = online;
        this.onBuy = onBuy;
        this.onRefund = onRefund;
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
    public List<StoreAction> getOnBuy() {
        return onBuy;
    }

    public List<StoreAction> getOnRefund() {
        return onRefund;
    }

    @SuppressWarnings("deprecation")
    public void buy(@NotNull String player, int orderID) {
        final StoreClient client = new StoreClient(Bukkit.getOfflinePlayer(player))
                .parser(s -> s
                        .replace("{order_player}", player)
                        .replace("{order_id}", String.valueOf(orderID))
                        .replace("{item_price}", price)
                );
        for (StoreAction action : onBuy) {
            action.run(client);
        }
    }

    @SuppressWarnings("deprecation")
    public void refund(@NotNull String player, int orderID) {
        final StoreClient client = new StoreClient(Bukkit.getOfflinePlayer(player))
                .parser(s -> s
                        .replace("{order_player}", player)
                        .replace("{order_id}", String.valueOf(orderID))
                        .replace("{item_price}", price)
                );
        for (StoreAction action : onRefund) {
            action.run(client);
        }
    }
}
