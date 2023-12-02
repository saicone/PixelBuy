package com.saicone.pixelbuy.module.hook;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PlayerProvider {

    private static PlayerProvider INSTANCE = new PlayerProvider();
    private static final Cache<String, UUID> ID_CACHE = CacheBuilder.newBuilder().expireAfterAccess(3L, TimeUnit.HOURS).build();
    private static final Cache<UUID, String> NAME_CACHE = CacheBuilder.newBuilder().expireAfterAccess(3L, TimeUnit.HOURS).build();
    private static final Map<String, Supplier<PlayerProvider>> SUPPLIERS = new LinkedHashMap<>();

    static {
        SUPPLIERS.put("LUCKPERMS", () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                return new LuckPermsProvider();
            }
            return null;
        });
    }

    public static void compute(@NotNull String type) {
        if (type.equalsIgnoreCase("AUTO")) {
            for (var entry : SUPPLIERS.entrySet()) {
                final PlayerProvider provider = entry.getValue().get();
                if (provider != null) {
                    INSTANCE = provider;
                    return;
                }
            }
        } else {
            for (var entry : SUPPLIERS.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(type)) {
                    final PlayerProvider provider = entry.getValue().get();
                    if (provider != null) {
                        INSTANCE = provider;
                        return;
                    }
                }
            }
        }
        INSTANCE = new PlayerProvider();
    }

    public static void supply(@NotNull String type, @NotNull Supplier<PlayerProvider> supplier) {
        SUPPLIERS.put(type, supplier);
    }

    @NotNull
    public static PlayerProvider get() {
        return INSTANCE;
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

    private static final class LuckPermsProvider extends PlayerProvider {

        private final LuckPerms luckPerms = net.luckperms.api.LuckPermsProvider.get();

        @Override
        public @NotNull UUID uniqueId(@NotNull String name) {
            final UUID uuid;
            try {
                uuid = luckPerms.getUserManager().lookupUniqueId(name).get();
            } catch (Throwable t) {
                return super.uniqueId(name);
            }
            return uuid == null ? super.uniqueId(name) : uuid;
        }

        @Override
        public @Nullable String name(@NotNull UUID uniqueId) {
            final String name;
            try {
                name = luckPerms.getUserManager().lookupUsername(uniqueId).get();
            } catch (Throwable t) {
                return super.name(uniqueId);
            }
            return name == null ? super.name(uniqueId) : name;
        }
    }
}
