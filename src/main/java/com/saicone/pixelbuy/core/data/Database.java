package com.saicone.pixelbuy.core.data;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.module.data.client.FileDatabase;
import com.saicone.pixelbuy.module.data.client.MySQLDatabase;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.data.DataClient;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private final PixelBuy plugin;

    private final List<StoreUser> cachedData = new ArrayList<>();

    private DataClient database;

    public Database(@NotNull PixelBuy plugin) {
        this.plugin = plugin;
        reload(Bukkit.getConsoleSender());
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!cachedData.isEmpty()) saveCachedData();
        }, 12000, 12000);
    }

    public void shut() {
        cachedData.clear();
    }

    public void reload(@NotNull CommandSender sender) {
        switch (PixelBuy.settings().getString("Database.Type", "JSON").toUpperCase()) {
            case "JSON":
                database = new FileDatabase();
                break;
            case "MYSQL":
                database = new MySQLDatabase();
                break;
            default:
                Lang.COMMAND_RELOAD_DATABASE_DEFAULT.sendTo(sender);
                database = new FileDatabase();
        }
        if (database.setup()) {
            Lang.COMMAND_RELOAD_DATABASE_DONE.sendTo(sender, getCurrentType());
        } else {
            Lang.COMMAND_RELOAD_DATABASE_ERROR.sendTo(sender, getCurrentType());
            setDefault();
        }
        if (PixelBuy.settings().getBoolean("Database.Convert-Data") && !getCurrentType().equals("JSON")) {
            convertData(sender, "JSON", true);
        }
    }

    public void setDefault() {
        database = new FileDatabase();
        database.setup();
    }

    public void convertData(@NotNull CommandSender sender, @NotNull String from, boolean delete) {
        from = from.toUpperCase();
        if (getCurrentType().equals(from)) {
            Lang.COMMAND_DATABASE_CONVERT_SAME_TYPE.sendTo(sender);
            return;
        }
        final DataClient base;
        switch (from) {
            case "JSON":
                base = new FileDatabase();
                break;
            case "MYSQL":
                base = new MySQLDatabase();
                break;
            default:
                Lang.COMMAND_DATABASE_CONVERT_UNKNOWN.sendTo(sender, from);
                return;
        }
        if (!from.equals("JSON")) {
            if (!base.setup()) {
                Lang.COMMAND_DATABASE_CONVERT_SETUP_ERROR.sendTo(sender, from);
                return;
            }
        }
        for (StoreUser user : base.getAllData()) {
            database.saveData(user);
            if (delete) {
                base.deleteData(user.getName().toLowerCase());
            }
        }
    }

    @NotNull
    public String getCurrentType() {
        return database.getType();
    }

    public void saveData(@Nullable StoreUser user) {
        if (user != null) {
            database.saveData(user);
        }
    }

    @Nullable
    public StoreUser getData(@NotNull String player) {
        player = player.toLowerCase();
        return database.getData(player);
    }

    public void deleteData(@NotNull String player) {
        player = player.toLowerCase();
        database.deleteData(player);
    }

    public void addCachedData(@NotNull StoreUser user) {
        cachedData.add(user);
    }

    public void saveCachedData() {
        cachedData.forEach(this::saveData);
        cachedData.clear();
    }
}
