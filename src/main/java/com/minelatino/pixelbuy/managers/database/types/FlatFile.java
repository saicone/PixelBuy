package com.minelatino.pixelbuy.managers.database.types;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.database.DatabaseType;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import org.bukkit.command.CommandSender;

public class FlatFile implements DatabaseType {

    private final PixelBuy pl = PixelBuy.get();

    public String getType() {
        return "JSON";
    }

    public void setup(CommandSender sender) {

    }

    public void saveData(CommandSender sender, PlayerData data) {

    }

    public PlayerData getData(CommandSender sender, String player) {

    }

    public void deleteData(CommandSender sender, String player) {

    }
}
