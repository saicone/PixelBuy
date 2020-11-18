package com.minelatino.pixelbuy.managers;

import com.minelatino.pixelbuy.PixelBuy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventManager implements Listener {

    private final PixelBuy pl = PixelBuy.get();

    public EventManager() {
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        pl.getPlayerManager().loadPlayer(e.getPlayer());
        pl.getPlayerManager().processPlayer(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        pl.getPlayerManager().unloadPlayer(e.getPlayer());
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }
}