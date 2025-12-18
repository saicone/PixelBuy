package com.saicone.pixelbuy;

import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import com.saicone.ezlib.Ezlib;
import com.saicone.ezlib.EzlibLoader;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelBuyCommand;
import com.saicone.pixelbuy.core.data.Database;
import com.saicone.pixelbuy.core.store.PixelStore;
import com.saicone.pixelbuy.module.hook.Placeholders;
import com.saicone.pixelbuy.module.hook.PlayerProvider;
import com.saicone.pixelbuy.module.settings.SettingsFile;
import com.saicone.pixelbuy.util.OptionalType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

@Dependencies(value = {
        @Dependency("com.github.cryptomorin:XSeries:13.5.1"),
        @Dependency("com.saicone.rtag:rtag-item:1.5.13"),
        @Dependency("com.saicone.nbt:nbt:1.0.2"),
        @Dependency("com.google.guava:guava:33.5.0-jre")
}, relocations = {
        "com.cryptomorin.xseries", "{package}.libs.xseries",
        "com.saicone.rtag", "{package}.libs.rtag",
        "com.saicone.nbt", "{package}.libs.nbt",
        "com.google.common", "{package}.libs.guava",
        // drivers
        "org.mariadb.jdbc", "{package}.libs.mariadb",
        "org.postgresql", "{package}.libs.postgresql",
        "org.h2", "{package}.libs.h2",
})
public final class PixelBuy extends JavaPlugin implements Executor {

    private static PixelBuy instance;

    private final EzlibLoader libraryLoader;
    private final SettingsFile settings;
    private final Lang lang;
    private final PixelStore store;
    private final Database database;
    private final PixelBuyCommand command;

    private List<String> placeholderNames;

    @NotNull
    public static PixelBuy get() {
        return instance;
    }

    @NotNull
    public static SettingsFile settings() {
        return get().getSettings();
    }

    public static void log(int level, @NotNull String msg, @Nullable Object... args) {
        get().getLang().sendLog(level, msg, args);
    }

    public static void logException(int level, @NotNull Throwable throwable) {
        get().getLang().printStackTrace(level, throwable);
    }

    public static void logException(int level, @NotNull Throwable throwable, @NotNull String msg, @Nullable Object... args) {
        get().getLang().printStackTrace(level, throwable, msg, args);
    }

    public PixelBuy() {
        final Ezlib ezlib = new Ezlib();
        ezlib.init();
        ezlib.dependency("com.google.code.gson:gson:2.11.0")
             .relocations(Map.of("com{}google{}gson".replace("{}", "."), "com.saicone.pixelbuy.libs.gson"))
             .parent(true)
             .load();
        libraryLoader = new EzlibLoader(EzlibLoader.class.getClassLoader(), null, ezlib).logger((level, msg) -> {
            switch (level) {
                case 1:
                    getLogger().severe(msg);
                    break;
                case 2:
                    getLogger().warning(msg);
                    break;
                case 3:
                    getLogger().info(msg);
                    break;
                default:
                    break;
            }
        }).replace("{package}", "com.saicone.pixelbuy").load();
        settings = new SettingsFile("settings.yml", true);
        lang = new Lang(this);
        store = new PixelStore();
        database = new Database(this);
        command = new PixelBuyCommand();
    }

    @Override
    public void onEnable() {
        instance = this;

        settings.loadFrom(getDataFolder(), true);
        lang.load();
        log(3, "Files loaded");

        store.onLoad();
        log(3, "Store loaded");

        database.onLoad();
        log(3, "Database loaded");

        PlayerProvider.supply("PIXELBUY", () -> {
           if (database.isUserLoadAll()) {
               return new PlayerProvider() {
                   @Override
                   public @NotNull UUID uniqueId(@NotNull String name) {
                       final UUID id = database.getUniqueId(name);
                       return id == null ? super.uniqueId(name) : id;
                   }

                   @Override
                   public @Nullable String name(@NotNull UUID uniqueId) {
                       var user = database.getCached().get(uniqueId);
                       return user == null ? super.name(uniqueId) : user.getName();
                   }
               };
           }
           return null;
        });
        onReloadSettings();

        command.onLoad(settings);
    }

    @Override
    public void onDisable() {
        if (placeholderNames != null) {
            Placeholders.unregister(placeholderNames);
            placeholderNames = null;
        }
        database.onDisable();
        store.onDisable();
    }

    public void onReload() {
        settings.loadFrom(getDataFolder(), true);
        lang.load();
        store.onLoad();
        database.onReload();
        onReloadSettings();
        command.onLoad(settings);
    }

    public void onReloadSettings() {
        PlayerProvider.compute(settings.getIgnoreCase("plugin", "playerprovider").asString("AUTO"));
        if (settings.getIgnoreCase("placeholder", "register").asBoolean(true)) {
            placeholderNames = Placeholders.register(this, settings.getIgnoreCase("placeholder", "names").asList(OptionalType::asString), (player, params) -> {
                params = params.toLowerCase();
                if (params.startsWith("top")) {
                    if (params.length() == 3) {
                        return player == null ? 0 : database.getIndex(player.getUniqueId()) + 1;
                    }
                    params = params.substring(4);
                    int i = params.indexOf('_');
                    if (i < 1) {
                        return null;
                    }
                    try {
                        final int index = Integer.parseInt(params.substring(0, i)) - 1;
                        final var cached = database.getCached(index);
                        return cached == null ? null : cached.get(params.substring(i + 1));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else {
                    return player == null ? null : database.getDataAsync(player.getUniqueId(), player.getName()).get(params);
                }
            });
        } else if (placeholderNames != null) {
            Placeholders.unregister(placeholderNames);
            placeholderNames = null;
        }
    }

    @NotNull
    public EzlibLoader getLibraryLoader() {
        return libraryLoader;
    }

    @NotNull
    public SettingsFile getSettings() {
        return settings;
    }

    @NotNull
    public Lang getLang() {
        return lang;
    }

    @NotNull
    public PixelStore getStore() {
        return store;
    }

    @NotNull
    public Database getDatabase() {
        return database;
    }

    @NotNull
    public PixelBuyCommand getCommand() {
        return command;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (!Bukkit.isPrimaryThread()) {
            command.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this, command);
        }
    }
}
