package com.minelatino.pixelbuy;

import java.util.UUID;

public interface PixelPlugin {

    default void onReload() { }

    UUID getPlayerUUID(String name);
}
