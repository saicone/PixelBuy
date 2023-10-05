package com.saicone.pixelbuy.api.event;

import com.saicone.pixelbuy.managers.player.PlayerData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OrderProcessedEvent extends Event implements Cancellable {

    private final String player;
    private final PlayerData.Order order;
    private boolean isCancelled;

    public OrderProcessedEvent(String player, PlayerData.Order order) {
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

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public String getPlayer() {
        return this.player;
    }

    public PlayerData.Order getOrder() {
        return order;
    }
}
