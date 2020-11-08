package com.minelatino.pixelbuy;

import com.minelatino.pixelbuy.command.MainCommand;
import com.minelatino.pixelbuy.managers.FilesManager;
import com.minelatino.pixelbuy.managers.database.DatabaseManager;
import com.minelatino.pixelbuy.managers.listener.EventManager;
import com.minelatino.pixelbuy.managers.order.OrderManager;
import com.minelatino.pixelbuy.managers.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
