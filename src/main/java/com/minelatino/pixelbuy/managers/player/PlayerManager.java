package com.minelatino.pixelbuy.managers.player;

import com.minelatino.pixelbuy.PixelBuy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private final PixelBuy pl = PixelBuy.get();

    private final Map<Player, PlayerData> players = new HashMap<>();

    public PlayerManager() {
        reload(true);
    }

    public void reload(boolean init) {

    }

    public void loadPlayers() {

    }

    public void loadPlayer() {

    }

    public void processPlayers() {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                PlayerData data = pl.getDatabase().getData(p.getName());
                if (data != null) {
                    data.getCommands().forEach(cmd -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
                    pl.getDatabase().deleteData(p.getName());
                }
            }
        });
    }

    public void processPlayer(Player player) {
        PlayerData data = pl.getDatabase().getData(player.getName());
        if (data != null) {
            data.getCommands().forEach(cmd -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
            pl.getDatabase().deleteData(player.getName());
        }
    }
}
