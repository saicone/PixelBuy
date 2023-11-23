package com.saicone.pixelbuy.module.hook;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.saicone.pixelbuy.PixelBuy;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerProvider {

    private static PlayerProvider INSTANCE = new PlayerProvider();
    private static final Cache<String, UUID> ID_CACHE = CacheBuilder.newBuilder().expireAfterAccess(3L, TimeUnit.HOURS).build();
    private static final Cache<UUID, String> NAME_CACHE = CacheBuilder.newBuilder().expireAfterAccess(3L, TimeUnit.HOURS).build();

    public static void compute(@NotNull String type) {
        switch (type.trim().toUpperCase()) {
            case "AUTO":
                if (PixelBuy.get().getDatabase().isUserLoadAll()) {
                    INSTANCE = new PixelBuyProvider();
                    return;
                }
                break;
            case "PIXELBUY":
                INSTANCE = new PixelBuyProvider();
                break;
            case "LUCKPERMS":
                if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                    INSTANCE = new LuckPermsProvider();
                    return;
                }
                break;
            default:
                break;
        }
        INSTANCE = new PlayerProvider();
    }

    @NotNull
    public static UUID getUniqueId(@NotNull String name) {
        UUID cached = ID_CACHE.getIfPresent(name);
        if (cached == null) {
            var player = Bukkit.getPlayer(name);
            if (player != null) {
                ID_CACHE.put(name, player.getUniqueId());
            } else {
                ID_CACHE.put(name, INSTANCE.uniqueId(name));
            }
            cached = ID_CACHE.getIfPresent(name);
        }
        return cached;
    }

    @Nullable
    public static String getName(@NotNull UUID uniqueId) {
        String cached = NAME_CACHE.getIfPresent(uniqueId);
        if (cached == null) {
            final String name;
            var player = Bukkit.getPlayer(uniqueId);
            if (player != null) {
                name = player.getName();
            } else {
                name = INSTANCE.name(uniqueId);
            }
            NAME_CACHE.put(uniqueId, name == null ? "" : name);
            cached = NAME_CACHE.getIfPresent(uniqueId);
        }
        return cached.isEmpty() ? null : cached;
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public UUID uniqueId(@NotNull String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    @Nullable
    public String name(@NotNull UUID uniqueId) {
        return Bukkit.getOfflinePlayer(uniqueId).getName();
    }

    private static final class PixelBuyProvider extends PlayerProvider {
        @Override
        public @NotNull UUID uniqueId(@NotNull String name) {
            final UUID id = PixelBuy.get().getDatabase().getUniqueId(name);
            return id == null ? super.uniqueId(name) : id;
        }

        @Override
        public @Nullable String name(@NotNull UUID uniqueId) {
            var user = PixelBuy.get().getDatabase().getCached().get(uniqueId);
            return user == null ? super.name(uniqueId) : user.getName();
        }
    }

    private static final class LuckPermsProvider extends PlayerProvider {

        private final LuckPerms luckPerms = net.luckperms.api.LuckPermsProvider.get();

        @Override
        public @NotNull UUID uniqueId(@NotNull String name) {
            final User user;
            try {
                user = luckPerms.getUserManager().getUser(name);
            } catch (Throwable t) {
                return super.uniqueId(name);
            }
            return user == null ? super.uniqueId(name) : user.getUniqueId();
        }

        @Override
        public @Nullable String name(@NotNull UUID uniqueId) {
            final User user;
            try {
                user = luckPerms.getUserManager().getUser(uniqueId);
            } catch (Throwable t) {
                return super.name(uniqueId);
            }
            return user == null ? super.name(uniqueId) : user.getUsername();
        }
    }
}
