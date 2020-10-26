package com.minelatino.pixelbuy.managers.listener.events;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> PlayerManager.loadPlayer(e.getPlayer()));
    }
}
