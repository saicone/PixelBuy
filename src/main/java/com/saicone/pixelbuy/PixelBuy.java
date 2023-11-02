package com.saicone.pixelbuy;

import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelBuyCommand;
import com.saicone.pixelbuy.core.data.Database;

import com.saicone.pixelbuy.core.store.PixelStore;
import com.saicone.pixelbuy.module.command.BukkitCommandNode;
import com.saicone.pixelbuy.module.hook.PlayerIdProvider;
import com.saicone.pixelbuy.module.settings.SettingsFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PixelBuy extends JavaPlugin {

    private static PixelBuy instance;

    private final SettingsFile settings;
    private final Lang lang;
    private final Database database;
    private final PixelStore store;
    private final PixelBuyCommand command;

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
        database = new Database();
        store = new PixelStore();
        command = new PixelBuyCommand();
    }

    @Override
    public void onEnable() {
        instance = this;

        settings.loadFrom(getDataFolder(), true);
        lang.load();
        log(4, "Files loaded");

        database.onLoad();
        log(4, "DatabaseManager loaded");

        store.onLoad();
        log(4, "Store loaded");

        PlayerIdProvider.compute(settings.getIgnoreCase("plugin", "uuidprovider").asString("AUTO"));

        command.onLoad(settings);
    }

    @Override
    public void onDisable() {
        store.onDisable();
        database.onDisable();
    }

    public void onReload() {
        settings.loadFrom(getDataFolder(), true);
        lang.load();
        database.onReload();
        store.onLoad();
        onReloadSettings();
        command.onLoad(settings);
    }

    public void onReloadSettings() {
        PlayerIdProvider.compute(settings.getIgnoreCase("plugin", "uuidprovider").asString("AUTO"));
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
    public PixelBuyCommand getCommand() {
        return command;
    }
}
