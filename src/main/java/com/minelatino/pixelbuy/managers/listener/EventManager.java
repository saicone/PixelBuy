package com.minelatino.pixelbuy.managers.listener;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.listener.events.PlayerJoin;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

public class EventManager {

    private final PixelBuy pl = PixelBuy.get();
    private final PlayerJoin playerJoin = new PlayerJoin();

    public EventManager() {
        registerEvents();
    }

    public void registerEvents() {
        PluginManager pm = pl.getServer().getPluginManager();
        pm.registerEvents(playerJoin, pl);
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(playerJoin);
    }
}
