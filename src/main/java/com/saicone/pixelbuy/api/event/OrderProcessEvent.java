package com.saicone.pixelbuy.api.event;

import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.api.store.StoreUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OrderProcessEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StoreUser user;
    private final StoreOrder order;
    private boolean isCancelled;

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public OrderProcessEvent(@NotNull StoreUser user, @NotNull StoreOrder order) {
        this.user = user;
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
    public StoreUser getUser() {
        return this.user;
    }

    @NotNull
    public StoreOrder getOrder() {
        return order;
    }
}
