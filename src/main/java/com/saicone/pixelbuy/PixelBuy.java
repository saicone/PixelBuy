package com.saicone.pixelbuy;

import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelBuyCommand;
import com.saicone.pixelbuy.core.web.WebType;
import com.saicone.pixelbuy.module.command.BukkitCommand;
import com.saicone.pixelbuy.module.listener.BukkitListener;
import com.saicone.pixelbuy.core.data.Database;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.core.UserCore;

import com.saicone.pixelbuy.core.PixelStore;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.module.settings.SettingsFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class PixelBuy extends JavaPlugin {

    private static PixelBuy instance;

    private final SettingsFile settings;
    private final Lang lang;
    private final Map<String, WebSupervisor> supervisors = new HashMap<>();

    private PixelStore store;
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

    public PixelBuy() {
        settings = new SettingsFile("settings.yml", true);
        lang = new Lang(this);
    }

    @Override
    public void onEnable() {
        instance = this;

        settings.loadFrom(getDataFolder(), true);
        lang.load();
        log(4, "Files loaded");

        store = new PixelStore();
        log(4, "StoreManager loaded");

        database = new Database(this);
        log(4, "DatabaseManager loaded");

        userCore = new UserCore();
        log(4, "PlayerManager loaded");

        reloadSupervisors();
        log(4, "WebSupervisors loaded");

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
        database.shut();
        store.shut();
        for (var entry : supervisors.entrySet()) {
            entry.getValue().onClose();
        }
    }

    public void onReload() {
        settings.loadFrom(getDataFolder(), true);
        lang.load();
        reloadSupervisors();
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

    @Nullable
    public WebSupervisor getSupervisor(@NotNull String id) {
        return supervisors.get(id);
    }

    @NotNull
    public Map<String, WebSupervisor> getSupervisors() {
        return supervisors;
    }

    @NotNull
    public UserCore getUserCore() {
        return userCore;
    }

    @NotNull
    public BukkitListener getListener() {
        return listener;
    }

    public void reloadSupervisors() {
        final BukkitSettings section = BukkitSettings.of(settings.getRegex("(?i)supervisors?").or(null));
        if (section == null) {
            for (var entry : supervisors.entrySet()) {
                entry.getValue().onClose();
            }
            return;
        }

        // Disable removed or reload current supervisors
        supervisors.entrySet().removeIf(entry -> {
            entry.getValue().onClose();
            if (section.contains(entry.getKey())) {
                final BukkitSettings config = section.getConfigurationSection(entry.getKey());
                if (config != null && WebType.of(config.getIgnoreCase("type").asString()) == entry.getValue().getType()) {
                    entry.getValue().onLoad(config);
                    entry.getValue().onStart();
                    return false;
                }
            }
            return true;
        });

        // Load added supervisors
        for (String key : section.getKeys(false)) {
            if (supervisors.containsKey(key)) {
                continue;
            }

            final BukkitSettings config = section.getConfigurationSection(key);
            if (config == null) {
                continue;
            }

            final WebType type = WebType.of(config.getIgnoreCase("type").asString());
            final WebSupervisor supervisor = type.newSupervisor(key);
            if (supervisor == null) {
                log(2, "Unknown web type for '" + key + "' supervisor");
                continue;
            }

            supervisor.onLoad(config);
            supervisor.onStart();
            supervisors.put(key, supervisor);
        }
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
