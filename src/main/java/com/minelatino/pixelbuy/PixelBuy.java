package com.minelatino.pixelbuy;

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

	public static PixelBuy get() {
		return pixelBuy;
	}

	@Override
	public void onEnable() {
		pixelBuy = this;

		filesManager = new FilesManager(Bukkit.getConsoleSender());
		databaseManager = new DatabaseManager();
		orderManager = new OrderManager();
		playerManager = new PlayerManager();
	}

	@Override
	public void onDisable() {

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
