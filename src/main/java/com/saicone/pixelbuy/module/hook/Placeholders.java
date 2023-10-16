package com.saicone.pixelbuy.module.hook;

import com.google.common.base.Suppliers;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Placeholders {

    private static final Supplier<Boolean> ENABLED = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"));

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    @Nullable
    @Contract("_, !null -> !null")
    public static String parse(@NotNull OfflinePlayer player, @Nullable String s) {
        if (s != null && isEnabled()) {
            return PlaceholderAPI.setPlaceholders(player, s);
        }
        return s;
    }

    @Nullable
    @Contract("_, !null -> !null")
    public static String parseBracket(@NotNull OfflinePlayer player, @Nullable String s) {
        if (s != null && isEnabled()) {
            return PlaceholderAPI.setBracketPlaceholders(player, s);
        }
        return s;
    }
}
