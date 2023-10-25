package com.saicone.pixelbuy;

import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelBuyCommand;
import com.saicone.pixelbuy.module.command.BukkitCommand;
import com.saicone.pixelbuy.module.listener.BukkitListener;
import com.saicone.pixelbuy.core.data.Database;
import com.saicone.pixelbuy.core.UserCore;

import com.saicone.pixelbuy.core.store.PixelStore;
import com.saicone.pixelbuy.module.settings.SettingsFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class PixelBuy extends JavaPlugin {

    private static PixelBuy instance;

    private final SettingsFile settings;
    private final Lang lang;
    private final PixelStore store;
    private Database database;

    private UserCore userCore;
    private BukkitListener listener;
    private PixelBuyCommand command;

    private final File folderData = new File(getDataFolder() + File.separator + "plugindata");

    @NotNull
    public static PixelBuy get() {
        return instance;
    }

    @NotNull
    public static SettingsFile settings() {
        return get().getSettings();
    }

    public static void log(int level, @NotNull String msg, @Nullable Object... args) {
        get().getLang().sendLog(level, msg, args);
    }

    public static void logException(int level, @NotNull Throwable throwable) {
        get().getLang().printStackTrace(level, throwable);
    }

    public static void logException(int level, @NotNull Throwable throwable, @NotNull String msg, @Nullable Object... args) {
        get().getLang().printStackTrace(level, throwable, msg, args);
    }

    public PixelBuy() {
        settings = new SettingsFile("settings.yml", true);
        lang = new Lang(this);
        store = new PixelStore();
        database = new Database();
    }

    @Override
    public void onEnable() {
        instance = this;

        settings.loadFrom(getDataFolder(), true);
        lang.load();
        log(4, "Files loaded");

        store.onLoad();
        log(4, "StoreManager loaded");

        database.onLoad();
        log(4, "DatabaseManager loaded");

        userCore = new UserCore();
        log(4, "PlayerManager loaded");

        listener = new BukkitListener();
        log(4, "EventManager loaded");

        registerCommand();
    }

    @Override
    public void onDisable() {
        log(3, "Disabling plugin...");
        unregisterCommand();
        listener.shut();
        userCore.shut();
        database.onDisable();
        store.onDisable();
    }

    public void onReload() {
        settings.loadFrom(getDataFolder(), true);
        lang.load();
        store.onLoad();
        database.onLoad();
        reloadCommand();
    }

    public File getFolderData() {
        if (!folderData.exists()) folderData.mkdir();
        return folderData;
    }

    @NotNull
    public SettingsFile getSettings() {
        return settings;
    }

    @NotNull
    public Lang getLang() {
        return lang;
    }

    @NotNull
    public PixelStore getStore() {
        return store;
    }

    @NotNull
    public Database getDatabase() {
        return database;
    }

    @NotNull
    public UserCore getUserCore() {
        return userCore;
    }

    @NotNull
    public BukkitListener getListener() {
        return listener;
    }

    public void reloadCommand() {
        unregisterCommand();
        registerCommand();
    }

    private void registerCommand() {
        command = new PixelBuyCommand(settings.getString("Commands.Main.Cmd", "pixelbuy"), settings.getStringList("Commands.Main.Aliases"));
        BukkitCommand.register(command);
        command.isRegistered();
    }

    private void unregisterCommand() {
        BukkitCommand.unregister(command);
    }
}
