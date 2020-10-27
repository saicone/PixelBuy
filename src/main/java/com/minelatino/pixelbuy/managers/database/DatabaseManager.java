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
                break;
            case "MYSQL":
                database = new MySQL();
                break;
        }
        if (database.setup()) {
            sender.sendMessage(pl.getFiles().getMessages().getString("").replace("%type%", database.getType()));
        } else {
            sender.sendMessage(pl.getFiles().getMessages().getString(""));
            database = new FlatFile();
        }
    }

    public String getCurrentType() {
        return database.getType();
    }

    public void saveData(PlayerData data) {
        database.saveData(data);
    }

    public PlayerData getData(String player) {
        return database.getData(player);
    }

    public void deleteData(String player) {
        database.deleteData(player);
    }
}
