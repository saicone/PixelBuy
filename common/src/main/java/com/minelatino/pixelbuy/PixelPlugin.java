package com.minelatino.pixelbuy;

import com.minelatino.pixelbuy.module.config.Settings;

import java.util.UUID;

public interface PixelPlugin {

    default void onReload() { }

    UUID getPlayerUUID(String name);

    Settings settingsOf(String path, String defPath, boolean requireDefault);
}
