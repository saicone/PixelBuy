package com.minelatino.pixelbuy.managers;

import com.minelatino.pixelbuy.PixelBuy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventManager implements Listener {

    public EventManager() {
        PixelBuy pl = PixelBuy.get();
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PixelBuy.get().getPlayerManager().processPlayer(e.getPlayer());
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }
}