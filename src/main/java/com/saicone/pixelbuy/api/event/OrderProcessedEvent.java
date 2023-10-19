package com.saicone.pixelbuy.api.event;

import com.saicone.pixelbuy.api.store.StoreOrder;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OrderProcessedEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String player;
    private final StoreOrder order;
    private boolean isCancelled;

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public OrderProcessedEvent(@NotNull String player, @NotNull StoreOrder order) {
        this.player = player;
        this.order = order;
        this.isCancelled = false;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public String getPlayer() {
        return this.player;
    }

    @NotNull
    public StoreOrder getOrder() {
        return order;
    }
}
