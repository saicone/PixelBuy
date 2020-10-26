package com.minelatino.pixelbuy.managers.database;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.database.types.FlatFile;
import com.minelatino.pixelbuy.managers.database.types.MySQL;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class DatabaseManager {

    private final PixelBuy pl = PixelBuy.get();

    private DatabaseType database;

    public DatabaseManager() {
        reload(Bukkit.getConsoleSender());
    }

    public void reload(CommandSender sender) {
        switch (pl.getFiles().getSettings().getString("Database.Type", "JSON").toUpperCase()) {
            case "JSON":
                database = new FlatFile();
            case "MYSQL":
                database = new MySQL();
        }
        database.setup(sender);
    }

    public String getCurrentType() {
        return database.getType();
    }

    public void saveData(CommandSender sender, PlayerData data) {
        database.saveData(sender, data);
    }

    public PlayerData getData(CommandSender sender, String player) {
        return database.getData(sender, player);
    }

    public void deleteData(CommandSender sender, String player) {
        database.deleteData(sender, player);
    }
}
