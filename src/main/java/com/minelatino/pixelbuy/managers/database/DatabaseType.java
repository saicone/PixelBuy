package com.minelatino.pixelbuy.managers.database;

import com.minelatino.pixelbuy.managers.player.PlayerData;
import org.bukkit.command.CommandSender;

public interface DatabaseType {

    void setup(CommandSender sender);

    String getType();

    void saveData(CommandSender sender, PlayerData data);

    PlayerData getData(CommandSender sender, String player);

    void deleteData(CommandSender sender, String player);
}
