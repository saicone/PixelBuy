package com.minelatino.pixelbuy.managers.player;

import com.minelatino.pixelbuy.PixelBuy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerManager {

    private final PixelBuy pl = PixelBuy.get();

    public void processPlayers() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (pl.getDatabase().getData(p.getName()) != null) {
                for (String cmd : pl.getDatabase().getData(p.getName()).getCommands()) {
                    Bukkit.getScheduler().runTaskAsynchronously(pl, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
                }
                pl.getDatabase().deleteData(p.getName());
            }
        }
    }
}
