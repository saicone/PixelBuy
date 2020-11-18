package com.minelatino.pixelbuy.managers.database;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.database.types.FlatFile;
import com.minelatino.pixelbuy.managers.database.types.MySQL;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import com.minelatino.pixelbuy.util.Utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private final PixelBuy pl;

    private DatabaseType database;

    private final List<PlayerData> cachedData = new ArrayList<>();

    public DatabaseManager(PixelBuy pl) {
        this.pl = pl;
        reload(Bukkit.getConsoleSender());
        Bukkit.getScheduler().runTaskTimerAsynchronously(pl, () -> {
            if (!cachedData.isEmpty()) saveCachedData();
        }, 12000, 12000);
    }

    public void reload(CommandSender sender) {
        switch (pl.getFiles().getConfig().getString("Database.Type", "JSON").toUpperCase()) {
            case "JSON":
                database = new FlatFile();
                break;
            case "MYSQL":
                database = new MySQL();
                break;
            default:
                sender.sendMessage(Utils.color(pl.langString("Command.Reload.Database.Default")));
                database = new FlatFile();
                break;
        }
        if (database.setup()) {
            sender.sendMessage(Utils.color(pl.langString("Command.Reload.Database.Success").replace("%type%", getCurrentType())));
        } else {
            sender.sendMessage(Utils.color(pl.langString("Command.Reload.Database.Error").replace("%type%", getCurrentType())));
            setDefault();
        }
        if (pl.configBoolean("Database.Convert-Data") && !getCurrentType().equals("JSON")) convertData(sender, "JSON", true);
    }

    public void setDefault() {
        database = new FlatFile();
        database.setup();
    }

    public void convertData(CommandSender sender, String from, boolean delete) {
        from = from.toUpperCase();
        if (getCurrentType().equals(from)) {
            sender.sendMessage(Utils.color(pl.langString("Command.Database.Convert.Same-Type")));
            return;
        }
        DatabaseType base;
        switch (from) {
            case "JSON":
                base = new FlatFile();
                break;
            case "MYSQL":
                base = new MySQL();
                break;
            default:
                sender.sendMessage(Utils.color(pl.langString("Command.Database.Convert.No-Exist")));
                return;
        }
        if (!from.equals("JSON")) {
            if (!base.setup()) {
                sender.sendMessage(Utils.color(pl.langString("Command.Database.Convert.Cant-Setup")));
                return;
            }
        }
        for (PlayerData data : base.getAllData()) {
            database.saveData(data);
            if (delete) base.deleteData(data.getPlayer().toLowerCase());
        }
    }

    public String getCurrentType() {
        return database.getType();
    }

    public void saveData(PlayerData data) {
        if (data != null) database.saveData(data);
    }

    public PlayerData getData(String player) {
        player = player.toLowerCase();
        return database.getData(player);
    }

    public void deleteData(String player) {
        player = player.toLowerCase();
        database.deleteData(player);
    }

    public void addCachedData(PlayerData data) {
        cachedData.add(data);
    }

    public void saveCachedData() {
        cachedData.forEach(this::saveData);
        cachedData.clear();
    }
}
