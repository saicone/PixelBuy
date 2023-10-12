package com.saicone.pixelbuy;

import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelBuyCommand;
import com.saicone.pixelbuy.module.command.BukkitCommand;
import com.saicone.pixelbuy.module.listener.BukkitListener;
import com.saicone.pixelbuy.core.data.Database;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.core.UserCore;

import com.saicone.pixelbuy.core.PixelStore;
import com.saicone.pixelbuy.module.settings.SettingsFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class PixelBuy extends JavaPlugin {

    private static PixelBuy instance;

    private final SettingsFile settings;
    private final Lang lang;

    private PixelStore store;
    private Database database;
    private WebSupervisor supervisor;
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

    public PixelBuy() {
        settings = new SettingsFile("settings.yml", true);
        lang = new Lang(this);
    }

    @Override
    public void onEnable() {
        instance = this;

        settings.loadFrom(getDataFolder(), true);
        lang.load();
        log(4, "FilesManager loaded");

        store = new PixelStore();
        log(4, "StoreManager loaded");

        database = new Database(this);
        log(4, "DatabaseManager loaded");

        userCore = new UserCore();
        log(4, "PlayerManager loaded");

        supervisor = new WebSupervisor();
        log(4, "OrderManager loaded");

        listener = new BukkitListener();
        log(4, "EventManager loaded");

        registerCommand();
    }

    @Override
    public void onDisable() {
        log(3, "Disabling plugin...");
        unregisterCommand();
        listener.shut();
        supervisor.shut();
        userCore.shut();
        database.shut();
        store.shut();
    }

    public void onReload() {
        settings.loadFrom(getDataFolder(), true);
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
    public WebSupervisor getSupervisor() {
        return supervisor;
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
