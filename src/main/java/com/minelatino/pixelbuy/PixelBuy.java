package com.minelatino.pixelbuy;

import com.minelatino.pixelbuy.command.MainCommand;
import com.minelatino.pixelbuy.managers.EventManager;
import com.minelatino.pixelbuy.managers.FilesManager;
import com.minelatino.pixelbuy.managers.database.DatabaseManager;
import com.minelatino.pixelbuy.managers.order.OrderManager;
import com.minelatino.pixelbuy.managers.player.PlayerManager;

import com.minelatino.pixelbuy.managers.store.StoreManager;
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
    private MainCommand mainCommand;

    private FilesManager filesManager;
    private StoreManager storeManager;
    private DatabaseManager databaseManager;
    private OrderManager orderManager;
    private PlayerManager playerManager;
    private EventManager eventManager;

    private final File folderData = new File(getDataFolder() + File.separator + "plugindata");

    public static PixelBuy get() {
        return pixelBuy;
    }

    @Override
    public void onEnable() {
        pixelBuy = this;

        filesManager = new FilesManager(Bukkit.getConsoleSender());
        getLogger().info(langString("Plugin.Init.FilesManager"));
        storeManager = new StoreManager();
        getLogger().info(langString("Plugin.Init.StoreManager"));
        databaseManager = new DatabaseManager(this);
        getLogger().info(langString("Plugin.Init.DatabaseManager"));
        playerManager = new PlayerManager();
        getLogger().info(langString("Plugin.Init.PlayerManager"));
        orderManager = new OrderManager();
        getLogger().info(langString("Plugin.Init.OrderManager"));
        eventManager = new EventManager();
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
        eventManager.shut();
        orderManager.shut();
        playerManager.shut();
        databaseManager.shut();
        storeManager.shut();
    }

    public String configString(String path) {
        return filesManager.getConfig().getString(path, "");
    }

    public int configInt(String path) {
        return filesManager.getConfig().getInt(path, 1000);
    }

    public boolean configBoolean(String path) {
        return filesManager.getConfig().getBoolean(path, false);
    }

    public String langString(String path) {
        return filesManager.getLang().getString(path, "");
    }

    public List<String> langStringList(String path) {
        return filesManager.getLang().getStringList(path);
    }

    public File getFolderData() {
        if (!folderData.exists()) folderData.mkdir();
        return folderData;
    }

    public FilesManager getFiles() {
        return filesManager;
    }

    public StoreManager getStore() {
        return storeManager;
    }

    public DatabaseManager getDatabase() {
        return databaseManager;
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public void reloadCommand() {
        unregisterCommand();
        registerCommand();
    }

    private void registerCommand() {
        if (commandMap != null) {
            mainCommand = new MainCommand(filesManager.getConfig().getString("Commands.Main.Cmd", "pixelbuy"), filesManager.getConfig().getStringList("Commands.Main.Aliases"));
            commandMap.register("pixelbuy", mainCommand);
            mainCommand.isRegistered();
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
                knownCmds.remove(mainCommand.getName());
                mainCommand.getAliases().forEach(alias -> {
                    if (knownCmds.containsKey(alias) && knownCmds.get(alias).toString().contains(mainCommand.getName())) {
                        knownCmds.remove(alias);
                    }
                });
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            mainCommand.unregister(commandMap);
        }
    }
}
