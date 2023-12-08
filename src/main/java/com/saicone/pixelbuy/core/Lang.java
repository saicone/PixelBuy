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

    public static final Value NO_PERM = new Value("NoPerm");
    public static final Value TEXT_YES = new Value("Text.True");
    public static final Value TEXT_NO = new Value("Text.False");
    public static final Value COMMAND_DISPLAY_SUB = new Value("Command.Display.Sub");
    public static final Value COMMAND_DISPLAY_USAGE = new Value("Command.Display.Usage");
    public static final Value COMMAND_DISPLAY_USER_INVALID = new Value("Command.Display.User.Invalid");
    public static final Value COMMAND_DISPLAY_USER_INFO = new Value("Command.Display.User.Info");
    public static final Value COMMAND_DISPLAY_ORDER_INVALID = new Value("Command.Display.Order.Invalid");
    public static final Value COMMAND_DISPLAY_ORDER_FORMAT = new Value("Command.Display.Order.Format");
    public static final Value COMMAND_DISPLAY_ORDER_INFO = new Value("Command.Display.Order.Info");
    public static final Value COMMAND_DISPLAY_ORDER_ITEM_INVALID = new Value("Command.Display.Order.Item.Invalid");
    public static final Value COMMAND_DISPLAY_ORDER_ITEM_INFO = new Value("Command.Display.Order.Item.Info");

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
