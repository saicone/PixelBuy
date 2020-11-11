package com.minelatino.pixelbuy;

import com.minelatino.pixelbuy.command.MainCommand;
import com.minelatino.pixelbuy.managers.FilesManager;
import com.minelatino.pixelbuy.managers.database.DatabaseManager;
import com.minelatino.pixelbuy.managers.listener.EventManager;
import com.minelatino.pixelbuy.managers.order.OrderManager;
import com.minelatino.pixelbuy.managers.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public final class PixelBuy extends JavaPlugin {

	private static PixelBuy pixelBuy;
	private FilesManager filesManager;
	private DatabaseManager databaseManager;
	private OrderManager orderManager;
	private PlayerManager playerManager;
	private EventManager eventManager;

	public static PixelBuy get() {
		return pixelBuy;
	}

	@Override
	public void onEnable() {
		pixelBuy = this;

		filesManager = new FilesManager(Bukkit.getConsoleSender());
		getLogger().info(filesManager.getLang().getString("Plugin.Init.FilesManager"));
		databaseManager = new DatabaseManager(this);
        getLogger().info(filesManager.getLang().getString("Plugin.Init.DatabaseManager"));
        playerManager = new PlayerManager();
        getLogger().info(filesManager.getLang().getString("Plugin.Init.PlayerManager"));
		orderManager = new OrderManager();
        getLogger().info(filesManager.getLang().getString("Plugin.Init.OrderManager"));
		eventManager = new EventManager();
        getLogger().info(filesManager.getLang().getString("Plugin.Init.EventManager"));
        getCommand("pixelbuy").setExecutor(new MainCommand());
	}

	@Override
	public void onDisable() {
        getLogger().info(filesManager.getLang().getString("Plugin.Shut"));
	    eventManager.unregisterEvents();
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

	public FilesManager getFiles() {
		return filesManager;
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
}
