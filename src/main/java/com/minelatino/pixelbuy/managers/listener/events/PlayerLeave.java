package com.minelatino.pixelbuy.managers.listener.events;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeave implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> PlayerManager.unloadPlayer(e.getPlayer()));
    }
}
