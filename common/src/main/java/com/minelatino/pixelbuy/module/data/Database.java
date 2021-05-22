package com.minelatino.pixelbuy.module.data;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.module.config.Settings;

public class Database {

    private static Settings config;

    public static void load() {
        config = PixelBuy.getPlugin().settingsOf("database.yml", "database.yml", false);
    }

    public static void reload() {
        config.reload();
    }

    public static Settings getConfig() {
        return config;
    }
}
