package com.minelatino.pixelbuy;

import com.minelatino.pixelbuy.api.PixelBuyAPI;
import com.minelatino.pixelbuy.api.action.BukkitExecutor;
import com.minelatino.pixelbuy.module.config.Settings;
import com.minelatino.pixelbuy.module.config.SettingsBukkit;
import com.minelatino.pixelbuy.module.locale.BukkitLocale;
import com.minelatino.pixelbuy.module.locale.user.BukkitUser;
import com.minelatino.pixelbuy.module.locale.user.UserType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PixelBuyBukkit extends JavaPlugin implements PixelPlugin {

    private static PixelBuyBukkit instance;
    private BukkitUser userType;

    public static PixelBuyBukkit get() {
        return instance;
    }

    @Override
    public void onLoad() {
        PixelBuy.init(this);
    }

    @Override
    public void onEnable() {
        instance = this;
        userType = new BukkitUser();
        PixelBuy.SETTINGS = new SettingsBukkit("settings.yml");
        PixelBuy.LOCALE = new BukkitLocale(new SettingsBukkit("lang/" + PixelBuy.SETTINGS.getString("Locale.Language") + ".yml", "lang/en_US.yml", false));
        PixelBuyAPI.setupExecutor(new BukkitExecutor());
    }

    @Override
    public void onDisable() {

    }

    public UserType<?> getUserType() {
        return userType;
    }

    @Override
    public UUID getPlayerUUID(String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    @Override
    public Settings settingsOf(String path, String defPath, boolean requireDefault) {
        return new SettingsBukkit(path, defPath, requireDefault);
    }
}
