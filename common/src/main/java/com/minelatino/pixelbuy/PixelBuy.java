package com.minelatino.pixelbuy;

import com.minelatino.pixelbuy.module.locale.PixelLocale;
import com.minelatino.pixelbuy.module.config.Settings;

public class PixelBuy {

    public static Settings SETTINGS;
    public static PixelLocale LOCALE;

    private static PixelPlugin plugin;

    public static void init(PixelPlugin pl) {
    	if (plugin == null) {
    		plugin = pl;
		}
	}

	public static void reload() {
    	SETTINGS.reload();
    	LOCALE.reload();
	}

	public static PixelPlugin getPlugin() {
		return plugin;
	}
}
