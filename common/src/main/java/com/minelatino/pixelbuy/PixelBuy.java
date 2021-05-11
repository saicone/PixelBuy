package com.minelatino.pixelbuy;

import com.minelatino.pixelbuy.module.locale.PixelLocale;
import com.minelatino.pixelbuy.module.config.Settings;

public class PixelBuy {

    public static Settings SETTINGS;
    public static PixelLocale LOCALE;

    public static void init() {

	}

	public static void reload() {
    	SETTINGS.reload();
    	LOCALE.reload();
	}
}
