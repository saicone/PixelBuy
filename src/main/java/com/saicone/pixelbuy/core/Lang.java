package com.saicone.pixelbuy.core;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.module.lang.LangLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lang extends LangLoader {

    public static final Value COMMAND_NO_PERM = new Value("Command.NoPermission");
    public static final Value COMMAND_HELP = new Value("Command.Help");
    public static final Value COMMAND_DATABASE_HELP = new Value("Command.Database.Help");
    public static final Value COMMAND_DATABASE_CONVERT_USAGE = new Value("Command.Database.Convert.Usage");
    public static final Value COMMAND_DATABASE_CONVERT_SAME_TYPE = new Value("Command.Database.Convert.SameType");
    public static final Value COMMAND_DATABASE_CONVERT_UNKNOWN = new Value("Command.Database.Convert.Unknown");
    public static final Value COMMAND_DATABASE_CONVERT_SETUP_ERROR = new Value("Command.Database.Convert.SetupError");
    public static final Value COMMAND_DATABASE_DELETE_USAGE = new Value("Command.Database.Delete.Usage");
    public static final Value COMMAND_DATABASE_DELETE_DONE = new Value("Command.Database.Delete.Done");
    public static final Value COMMAND_PLAYERDATA_HELP = new Value("Command.PlayerData.Help");
    public static final Value COMMAND_PLAYERDATA_INFO_USAGE = new Value("Command.PlayerData.Info.Usage");
    public static final Value COMMAND_PLAYERDATA_INFO_UNKNOWN = new Value("Command.PlayerData.Info.Unknown");
    public static final Value COMMAND_PLAYERDATA_INFO_PLAYER = new Value("Command.PlayerData.Info.Player");
    public static final Value COMMAND_PLAYERDATA_INFO_ORDER = new Value("Command.PlayerData.Info.Order");
    public static final Value COMMAND_PLAYERDATA_INFO_ITEMS = new Value("Command.PlayerData.Info.Items");
    public static final Value COMMAND_PLAYERDATA_REFUND_USAGE = new Value("Command.PlayerData.Refund.Usage");
    public static final Value COMMAND_PLAYERDATA_REFUND_DONE = new Value("Command.PlayerData.Refund.Done");
    public static final Value COMMAND_PLAYERDATA_REFUND_ERROR = new Value("Command.PlayerData.Refund.Error");
    public static final Value COMMAND_PLAYERDATA_ORDER_USAGE = new Value("Command.PlayerData.Order.Usage");
    public static final Value COMMAND_PLAYERDATA_ORDER_DONE = new Value("Command.PlayerData.Order.Done");
    public static final Value COMMAND_PLAYERDATA_RECOVER_USAGE = new Value("Command.PlayerData.Recover.Usage");
    public static final Value COMMAND_PLAYERDATA_RECOVER_UNKNOWN = new Value("Command.PlayerData.Recover.Unknown");
    public static final Value COMMAND_PLAYERDATA_RECOVER_DONE = new Value("Command.PlayerData.Recover.Done");
    public static final Value COMMAND_RELOAD_HELP = new Value("Command.Reload.Help");
    public static final Value COMMAND_RELOAD_FILES = new Value("Command.Reload.Files");
    public static final Value COMMAND_RELOAD_DATABASE_DEFAULT = new Value("Command.Reload.Database.Default");
    public static final Value COMMAND_RELOAD_DATABASE_DONE = new Value("Command.Reload.Database.Done");
    public static final Value COMMAND_RELOAD_DATABASE_ERROR = new Value("Command.Reload.Database.Error");
    public static final Value COMMAND_RELOAD_WEBDATA = new Value("Command.Reload.Webdata");
    public static final Value COMMAND_RELOAD_COMMAND = new Value("Command.Reload.Command");
    public static final Value COMMAND_STATUS = new Value("Command.Status");
    public static final Value COMMAND_STORE_HELP = new Value("Command.Store.Help");
    public static final Value COMMAND_STORE_ITEMS_INFO = new Value("Command.Store.Items.Info");
    public static final Value COMMAND_STORE_ITEMS_ENUM = new Value("Command.Store.Items.Enum");
    public static final Value STATUS_ON = new Value("Status.On");
    public static final Value STATUS_OFF = new Value("Status.Off");
    public static final Value STATUS_IN_PROCESS = new Value("Status.InProcess");
    public static final Value STATUS_PENDING = new Value("Status.Pending");
    public static final Value STATUS_SENT = new Value("Status.Sent");
    public static final Value STATUS_REFUNDED = new Value("Status.Refunded");

    private int logLevel;
    private Map<String, String> languageAliases;
    private String pluginLanguage;
    private String defaultLanguage;

    private final List<String> defaultLanguages = List.of("en_US", "es_ES");

    public Lang(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public void load(@NotNull File langFolder) {
        logLevel = PixelBuy.settings().getIgnoreCase("plugin", "loglevel").asInt(3);
        if (languageAliases != null) {
            languageAliases.clear();
        } else {
            languageAliases = new HashMap<>();
        }
        final ConfigurationSection section = PixelBuy.settings().getConfigurationSection(settings -> settings.getIgnoreCase("lang", "aliases"));
        if (section != null) {
            for (String key : section.getKeys(false)) {
                final Object aliases = section.get(key);
                if (aliases instanceof List) {
                    for (Object alias : (List<?>) aliases) {
                        languageAliases.put(key.toLowerCase(), String.valueOf(alias).toLowerCase());
                    }
                } else if (aliases != null) {
                    languageAliases.put(key.toLowerCase(), String.valueOf(aliases).toLowerCase());
                }
            }
        }
        pluginLanguage = PixelBuy.settings().getIgnoreCase("plugin", "language").asString("en_US");
        defaultLanguage = PixelBuy.settings().getIgnoreCase("lang", "default").asString("en_US");
        super.load(langFolder);
    }

    @Override
    protected @NotNull Map<String, Object> getFileObjects(@NotNull File file) {
        final Map<String, Object> map = new HashMap<>();
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
            for (String path : config.getKeys(true)) {
                map.put(path, config.get(path));
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public @NotNull Map<String, String> getLanguageAliases() {
        return languageAliases;
    }

    @NotNull
    @Override
    public String getPluginLanguage() {
        return pluginLanguage;
    }

    @NotNull
    @Override
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    @NotNull
    @Override
    public List<String> getDefaultLanguages() {
        return defaultLanguages;
    }
}
