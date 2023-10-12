package com.saicone.pixelbuy;

import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelBuyCommand;
import com.saicone.pixelbuy.module.listener.BukkitListener;
import com.saicone.pixelbuy.core.data.Database;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.core.UserCore;

import com.saicone.pixelbuy.core.PixelStore;
import com.saicone.pixelbuy.module.settings.SettingsFile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

public final class PixelBuy extends JavaPlugin {

    private static PixelBuy pixelBuy;

    private CommandMap commandMap;
    private PixelBuyCommand pixelBuyCommand;

    private final SettingsFile settings;
    private final Lang lang;
    private PixelStore pixelStore;
    private Database database;
    private WebSupervisor webSupervisor;
    private UserCore userCore;
    private BukkitListener bukkitListener;

    private final File folderData = new File(getDataFolder() + File.separator + "plugindata");

    @NotNull
    public static PixelBuy get() {
        return pixelBuy;
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
        pixelBuy = this;

        settings.loadFrom(getDataFolder(), true);
        lang.load();
        log(4, "FilesManager loaded");

        pixelStore = new PixelStore();
        log(4, "StoreManager loaded");

        database = new Database(this);
        log(4, "DatabaseManager loaded");

        userCore = new UserCore();
        log(4, "PlayerManager loaded");

        webSupervisor = new WebSupervisor();
        log(4, "OrderManager loaded");

        bukkitListener = new BukkitListener();
        log(4, "EventManager loaded");

        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        registerCommand();
    }

    @Override
    public void onDisable() {
        log(3, "Disabling plugin...");
        unregisterCommand();
        bukkitListener.shut();
        webSupervisor.shut();
        userCore.shut();
        database.shut();
        pixelStore.shut();
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

    public PixelStore getStore() {
        return pixelStore;
    }

    public Database getDatabase() {
        return database;
    }

    public WebSupervisor getOrderManager() {
        return webSupervisor;
    }

    public UserCore getPlayerManager() {
        return userCore;
    }

    public BukkitListener getEventManager() {
        return bukkitListener;
    }

    public void reloadCommand() {
        unregisterCommand();
        registerCommand();
    }

    private void registerCommand() {
        if (commandMap != null) {
            pixelBuyCommand = new PixelBuyCommand(settings.getString("Commands.Main.Cmd", "pixelbuy"), settings.getStringList("Commands.Main.Aliases"));
            commandMap.register("pixelbuy", pixelBuyCommand);
            pixelBuyCommand.isRegistered();
        }
    }

    private void unregisterCommand() {
        if (commandMap != null) {
            try {
                Class<? extends CommandMap> cmdMapClass = commandMap.getClass();
                final Field field;
                if (cmdMapClass.getSimpleName().equals("CraftCommandMap")) {
                    field = cmdMapClass.getSuperclass().getDeclaredField("knownCommands");
                } else {
                    field = cmdMapClass.getDeclaredField("knownCommands");
                }
                field.setAccessible(true);

                Map<String, Command> knownCmds = (Map<String, Command>) field.get(commandMap);
                knownCmds.remove(pixelBuyCommand.getName());
                pixelBuyCommand.getAliases().forEach(alias -> {
                    if (knownCmds.containsKey(alias) && knownCmds.get(alias).toString().contains(pixelBuyCommand.getName())) {
                        knownCmds.remove(alias);
                    }
                });
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            pixelBuyCommand.unregister(commandMap);
        }
    }
}
