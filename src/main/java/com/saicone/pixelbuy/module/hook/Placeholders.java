package com.saicone.pixelbuy.module.hook;

import com.google.common.base.Suppliers;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.BiFunction;
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

    public static <T extends Collection<String>> T register(@NotNull Plugin plugin, @NotNull T names, @NotNull BiFunction<Player, String, Object> onPlaceholderRequest) {
        if (isEnabled()) {
            for (String name : names) {
                new Expansion(name, plugin) {

                    @Override
                    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
                        final Object obj = onPlaceholderRequest.apply(player, params);
                        return obj == null ? null : obj.toString();
                    }
                }.register();
            }
        }
        return names;
    }

    public static <T extends Collection<String>> T registerOffline(@NotNull Plugin plugin, @NotNull T names, @NotNull BiFunction<OfflinePlayer, String, Object> function) {
        if (isEnabled()) {
            for (String name : names) {
                new Expansion(name, plugin) {
                    @Override
                    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
                        final Object obj = function.apply(player, params);
                        return obj == null ? null : obj.toString();
                    }
                }.register();
            }
        }
        return names;
    }

    public static <T extends Collection<String>> T unregister(@NotNull T names) {
        if (isEnabled()) {
            for (String name : names) {
                final PlaceholderExpansion expansion = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion(name);
                if (expansion != null) {
                    expansion.unregister();
                }
            }
        }
        return names;
    }

    public static class Expansion extends PlaceholderExpansion {

        private final String name;
        private final String author;
        private final String version;

        public Expansion(@NotNull String name, @NotNull Plugin plugin) {
            this.name = name;
            this.author = String.join(", ", plugin.getDescription().getAuthors());
            this.version = plugin.getDescription().getVersion();
        }

        public Expansion(@NotNull String name, @NotNull String author, @NotNull String version) {
            this.name = name;
            this.author = author;
            this.version = version;
        }

        @Override
        public @NotNull String getIdentifier() {
            return name;
        }

        @Override
        public @NotNull String getAuthor() {
            return author;
        }

        @Override
        public @NotNull String getVersion() {
            return version;
        }

        @Override
        public boolean persist() {
            return true;
        }
    }
}
