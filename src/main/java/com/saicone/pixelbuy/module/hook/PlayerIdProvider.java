package com.saicone.pixelbuy.module.hook;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.saicone.pixelbuy.PixelBuy;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerIdProvider {

    private static PlayerIdProvider INSTANCE = new PlayerIdProvider();
    private static final Cache<String, UUID> CACHE = CacheBuilder.newBuilder().expireAfterAccess(3L, TimeUnit.HOURS).build();

    public static void compute(@NotNull String type) {
        switch (type.trim().toUpperCase()) {
            case "AUTO":
                if (PixelBuy.get().getDatabase().isUserLoadAll()) {
                    INSTANCE = new PixelBuyProvider();
                } else {
                    INSTANCE = new PlayerIdProvider();
                }
                break;
            case "PIXELBUY":
                INSTANCE = new PixelBuyProvider();
                break;
            default:
                INSTANCE = new PlayerIdProvider();
                break;
        }
    }

    @NotNull
    public static UUID getUniqueId(@NotNull String name) {
        UUID cached = CACHE.getIfPresent(name);
        if (cached == null) {
            CACHE.put(name, INSTANCE.get(name));
            cached = CACHE.getIfPresent(name);
        }
        return cached;
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public UUID get(@NotNull String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    private static final class PixelBuyProvider extends PlayerIdProvider {
        @Override
        public @NotNull UUID get(@NotNull String name) {
            final UUID id = PixelBuy.get().getDatabase().getUniqueId(name);
            return id == null ? super.get(name) : id;
        }
    }
}
