package com.saicone.pixelbuy.core.data;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.module.data.client.FileDatabase;
import com.saicone.pixelbuy.module.data.client.MySQLDatabase;
import com.saicone.pixelbuy.api.object.StoreUser;
import com.saicone.pixelbuy.module.data.DataClient;
import com.saicone.pixelbuy.util.Utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private final PixelBuy pl;

    private DataClient database;

    private final List<StoreUser> cachedData = new ArrayList<>();

    public Database(PixelBuy pl) {
        this.pl = pl;
        reload(Bukkit.getConsoleSender());
        Bukkit.getScheduler().runTaskTimerAsynchronously(pl, () -> {
            if (!cachedData.isEmpty()) saveCachedData();
        }, 12000, 12000);
    }

    public void shut() {
        cachedData.clear();
    }

    public void reload(CommandSender sender) {
        switch (pl.getFiles().getConfig().getString("Database.Type", "JSON").toUpperCase()) {
            case "JSON":
                database = new FileDatabase();
                break;
            case "MYSQL":
                database = new MySQLDatabase();
                break;
            default:
                sender.sendMessage(Utils.color(pl.langString("Command.Reload.Database.Default")));
                database = new FileDatabase();
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
        database = new FileDatabase();
        database.setup();
    }

    public void convertData(CommandSender sender, String from, boolean delete) {
        from = from.toUpperCase();
        if (getCurrentType().equals(from)) {
            sender.sendMessage(Utils.color(pl.langString("Command.Database.Convert.Same-Type")));
            return;
        }
        DataClient base;
        switch (from) {
            case "JSON":
                base = new FileDatabase();
                break;
            case "MYSQL":
                base = new MySQLDatabase();
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
        for (StoreUser data : base.getAllData()) {
            database.saveData(data);
            if (delete) base.deleteData(data.getPlayer().toLowerCase());
        }
    }

    public String getCurrentType() {
        return database.getType();
    }

    public void saveData(StoreUser data) {
        if (data != null) database.saveData(data);
    }

    public StoreUser getData(String player) {
        player = player.toLowerCase();
        return database.getData(player);
    }

    public void deleteData(String player) {
        player = player.toLowerCase();
        database.deleteData(player);
    }

    public void addCachedData(StoreUser data) {
        cachedData.add(data);
    }

    public void saveCachedData() {
        cachedData.forEach(this::saveData);
        cachedData.clear();
    }
}
