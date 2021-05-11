package com.minelatino.pixelbuy;

import com.minelatino.pixelbuy.api.PixelBuyAPI;
import com.minelatino.pixelbuy.api.action.BungeeExecutor;
import com.minelatino.pixelbuy.module.config.SettingsBungee;
import com.minelatino.pixelbuy.module.locale.BungeeLocale;
import com.minelatino.pixelbuy.module.locale.user.BungeeUser;
import com.minelatino.pixelbuy.module.locale.user.UserType;
import net.md_5.bungee.api.plugin.Plugin;

public class PixelBuyBungee extends Plugin {

    private static PixelBuyBungee instance;

    private BungeeUser userType;
    private boolean runtimeError = false;

    public static PixelBuyBungee get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        userType = new BungeeUser();
        PixelBuy.SETTINGS = new SettingsBungee("settings.yml");
        if (runtimeError) return;
        PixelBuy.LOCALE = new BungeeLocale(this, new SettingsBungee("lang/" + PixelBuy.SETTINGS.getString("Locale.Language") + ".yml", "lang/en_US.yml", false));
        PixelBuyAPI.setupExecutor(new BungeeExecutor());
    }

    @Override
    public void onDisable() {

    }

    public void setRuntimeError(boolean runtimeError) {
        this.runtimeError = runtimeError;
    }

    public UserType<?> getUserType() {
        return userType;
    }
}
