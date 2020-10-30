package com.minelatino.pixelbuy.managers.listener.events;

import com.minelatino.pixelbuy.PixelBuy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PixelBuy.get().getPlayerManager().processPlayer(e.getPlayer());
    }
}
