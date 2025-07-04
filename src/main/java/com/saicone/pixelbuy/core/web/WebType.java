package com.saicone.pixelbuy.core.web;

import com.saicone.pixelbuy.core.web.supervisor.PixelBuyWeb;
import com.saicone.pixelbuy.core.web.supervisor.WooMinecraftWeb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum WebType {

    UNKNOWN,
    PIXELBUY("CUSTOM"),
    WOO_MINECRAFT("WOOMINECRAFT");

    public static final WebType[] VALUES = values();

    private final String[] aliases;

    WebType(@NotNull String... aliases) {
        this.aliases = aliases;
    }

    @NotNull
    public String[] getAliases() {
        return aliases;
    }

    @Nullable
    public WebSupervisor newSupervisor(@NotNull String id, @NotNull String group) {
        if (this == PIXELBUY) {
            return new PixelBuyWeb(id, group);
        }
        if (this == WOO_MINECRAFT) {
            return new WooMinecraftWeb(id, group);
        }
        return null;
    }

    @NotNull
    public static WebType of(@Nullable String s) {
        if (s != null) {
            for (WebType value : VALUES) {
                if (value.name().equalsIgnoreCase(s)) {
                    return value;
                }
                for (String alias : value.aliases) {
                    if (alias.equalsIgnoreCase(s)) {
                        return value;
                    }
                }
            }
        }
        return WebType.UNKNOWN;
    }
}
