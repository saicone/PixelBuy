package com.saicone.pixelbuy;

import com.saicone.pixelbuy.core.command.PixelBuyCommand;
import com.saicone.pixelbuy.module.listener.BukkitListener;
import com.saicone.pixelbuy.module.settings.YamlSettings;
import com.saicone.pixelbuy.core.data.Database;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.core.UserCore;

import com.saicone.pixelbuy.core.PixelStore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public final class PixelBuy extends JavaPlugin {

    private static PixelBuy pixelBuy;
    private CommandMap commandMap;
    private PixelBuyCommand pixelBuyCommand;

    private YamlSettings yamlSettings;
    private PixelStore pixelStore;
    private Database database;
    private WebSupervisor webSupervisor;
    private UserCore userCore;
    private BukkitListener bukkitListener;

    private final File folderData = new File(getDataFolder() + File.separator + "plugindata");

    public static PixelBuy get() {
        return pixelBuy;
    }

    @Override
    public void onEnable() {
        pixelBuy = this;

        yamlSettings = new YamlSettings(Bukkit.getConsoleSender());
        getLogger().info(langString("Plugin.Init.FilesManager"));
        pixelStore = new PixelStore();
        getLogger().info(langString("Plugin.Init.StoreManager"));
        database = new Database(this);
        getLogger().info(langString("Plugin.Init.DatabaseManager"));
        userCore = new UserCore();
        getLogger().info(langString("Plugin.Init.PlayerManager"));
        webSupervisor = new WebSupervisor();
        getLogger().info(langString("Plugin.Init.OrderManager"));
        bukkitListener = new BukkitListener();
        getLogger().info(langString("Plugin.Init.EventManager"));

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
        getLogger().info(langString("Plugin.Shut"));
        unregisterCommand();
        bukkitListener.shut();
        webSupervisor.shut();
        userCore.shut();
        database.shut();
        pixelStore.shut();
    }

    public String configString(String path) {
        return yamlSettings.getConfig().getString(path, "");
    }

    public int configInt(String path) {
        return yamlSettings.getConfig().getInt(path, 1000);
    }

    public boolean configBoolean(String path) {
        return yamlSettings.getConfig().getBoolean(path, false);
    }

    public String langString(String path) {
        return yamlSettings.getLang().getString(path, "");
    }

    public String langString(String path, String def) {
        return yamlSettings.getLang().getString(path, def);
    }

    public List<String> langStringList(String path) {
        return yamlSettings.getLang().getStringList(path);
    }

    public File getFolderData() {
        if (!folderData.exists()) folderData.mkdir();
        return folderData;
    }

    public YamlSettings getFiles() {
        return yamlSettings;
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
            pixelBuyCommand = new PixelBuyCommand(yamlSettings.getConfig().getString("Commands.Main.Cmd", "pixelbuy"), yamlSettings.getConfig().getStringList("Commands.Main.Aliases"));
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
