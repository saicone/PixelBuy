package com.minelatino.pixelbuy;

import com.minelatino.pixelbuy.module.config.SettingsBukkit;
import com.minelatino.pixelbuy.module.locale.BukkitLocale;
import com.minelatino.pixelbuy.module.locale.user.BukkitUser;
import com.minelatino.pixelbuy.module.locale.user.UserType;
import org.bukkit.plugin.java.JavaPlugin;

public class PixelBuyBukkit extends JavaPlugin {

    private static PixelBuyBukkit instance;
    private BukkitUser userType;

    public static PixelBuyBukkit get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        userType = new BukkitUser();
        PixelBuy.SETTINGS = new SettingsBukkit("settings.yml");
        PixelBuy.LOCALE = new BukkitLocale(new SettingsBukkit("lang/" + PixelBuy.SETTINGS.getString("Locale.Language") + ".yml", "lang/en_US.yml", false));
    }

    @Override
    public void onDisable() {

    }

    public UserType<?> getUserType() {
        return userType;
    }
}
