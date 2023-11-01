package com.saicone.pixelbuy.module.lang;

import com.saicone.pixelbuy.util.MStrings;
import com.saicone.pixelbuy.util.OptionalType;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class LangLoader implements Listener {

    protected static final String DEFAULT_LANGUAGE = "en_us";

    protected static final List<String> DEFAULT_LANGUAGES = List.of("en_US");

    protected static final Map<String, String> LANGUAGE_ALIASES = Map.of(
            "en_au", "en_us",
            "en_ca", "en_us",
            "en_gb", "en_us",
            "en_nz", "en_us",
            "es_ar", "es_es",
            "es_cl", "es_es",
            "es_ec", "es_es",
            "es_mx", "es_es",
            "es_uy", "es_es",
            "es_ve", "es_es"
    );

    private final Plugin plugin;

    private Value[] paths = new Value[0];

    protected String defaultLanguage = null;
    protected final Map<String, String> languageAliases = new HashMap<>();
    protected final Map<String, String> playerLanguages = new HashMap<>();

    protected String filePrefix = ".yml";
    protected final Map<String, Map<String, List<String>>> displays = new HashMap<>();

    public LangLoader(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        load(new File(plugin.getDataFolder(), "lang"));
    }

    public void load(@NotNull File langFolder) {
        defaultLanguage = getDefaultLanguage().toLowerCase();
        languageAliases.clear();
        languageAliases.putAll(getLanguageAliases());

        if (!langFolder.exists()) {
            langFolder.mkdirs();

        }
        computePaths();

        final Map<String, List<File>> langFiles = getLangFiles(langFolder);
        for (String defaultLanguage : getDefaultLanguages()) {
            final String key = defaultLanguage.toLowerCase();
            if (!langFiles.containsKey(key)) {
                final File file = saveDefaultLang(langFolder, defaultLanguage);
                if (file != null) {
                    final List<File> list = new ArrayList<>();
                    list.add(file);
                    langFiles.put(key, list);
                }
            }
        }

        langFiles.forEach((key, list) -> list.forEach(file -> loadDisplays(key, file)));
    }

    protected void loadDisplays(@NotNull String name, @NotNull File file) {
        String prefix = null;
        for (var entry : getObjects(file).entrySet()) {
            if (entry.getKey().equalsIgnoreCase("prefix") && entry.getValue() instanceof String) {
                prefix = (String) entry.getValue();
            }
            final List<String> display = loadDisplay(entry.getValue());
            if (display != null && !display.isEmpty()) {
                if (!displays.containsKey(name)) {
                    displays.put(name, new HashMap<>());
                }
                final Map<String, List<String>> map = displays.get(name);
                map.put(entry.getKey().toLowerCase(), display);
            }
        }
        if (prefix != null && displays.containsKey(name)) {
            for (var entry : displays.get(name).entrySet()) {
                final List<String> display = entry.getValue();
                for (int i = 0; i < display.size(); i++) {
                    final String s = display.get(i);
                    if (s.contains("{prefix}")) {
                        display.set(i, s.replace("{prefix}", prefix));
                    }
                }
            }
        }
    }

    @Nullable
    protected List<String> loadDisplay(@Nullable Object object) {
        return object == null ? null : OptionalType.of(object).asList(type -> {
            final String s = type.asString();
            return s == null ? null : MStrings.color(s);
        });
    }

    private void computePaths() {
        if (paths.length > 0) {
            return;
        }
        final List<Value> paths = new ArrayList<>();
        // Check every superclass
        for (Class<?> clazz = getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.canAccess(null) && Value.class.isAssignableFrom(field.getType())) {
                    try {
                        final Value path = (Value) field.get(null);
                        path.setLoader(this);
                        paths.add(path);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        this.paths = paths.toArray(new Value[0]);
    }

    public void unload() {
        languageAliases.clear();
        playerLanguages.clear();
        for (var entry : displays.entrySet()) {
            entry.getValue().clear();
        }
        displays.clear();
    }

    @Nullable
    protected File saveDefaultLang(@NotNull File folder, @NotNull String name) {
        try {
            plugin.saveResource("lang/" + name + getFilePrefix(), false);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return new File(folder, name + getFilePrefix());
    }

    public void setFilePrefix(@NotNull String filePrefix) {
        this.filePrefix = filePrefix;
    }

    @NotNull
    protected Map<String, List<File>> getLangFiles(@NotNull File langFolder) {
        final Map<String, List<File>> map = new HashMap<>();
        final File[] files = langFolder.listFiles();
        if (files == null) {
            return map;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                getLangFiles(langFolder).forEach((key, list) -> map.computeIfAbsent(key, s -> new ArrayList<>()).addAll(list));
            } else {
                final int index = file.getName().lastIndexOf('.');
                final String name = index >= 1 ? file.getName().substring(0, index) : file.getName();
                map.computeIfAbsent(name.toLowerCase(), s -> new ArrayList<>()).add(file);
            }
        }
        return map;
    }

    @NotNull
    private Map<String, Object> getObjects(@NotNull File file) {
        return getFileObjects(file);
    }

    @NotNull
    protected abstract Map<String, Object> getFileObjects(@NotNull File file);

    public int getLogLevel() {
        return 2;
    }

    @NotNull
    public Value[] getPaths() {
        return paths;
    }

    @NotNull
    public String getLanguage(@NotNull String lang) {
        if (displays.containsKey(lang)) {
            return lang;
        } else {
            return languageAliases.getOrDefault(lang, defaultLanguage);
        }
    }

    @NotNull
    public String getLanguage(@NotNull CommandSender sender) {
        if (sender instanceof Player) {
            return getPlayerLanguage((Player) sender);
        } else {
            return getPluginLanguage();
        }
    }

    @NotNull
    public Map<String, String> getLanguageAliases() {
        return LANGUAGE_ALIASES;
    }

    @NotNull
    public String getPluginLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @NotNull
    public String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @NotNull
    public List<String> getDefaultLanguages() {
        return DEFAULT_LANGUAGES;
    }

    @NotNull
    public String getPlayerLanguage(@NotNull Player player) {
        final String name = player.getName();
        if (!playerLanguages.containsKey(name)) {
            playerLanguages.put(name, getLanguage(player.getLocale().toLowerCase()));
        }
        return playerLanguages.get(name);
    }

    @NotNull
    public Map<String, String> getPlayerLanguages() {
        return playerLanguages;
    }

    @NotNull
    public String getFilePrefix() {
        return filePrefix;
    }

    @NotNull
    public Map<String, Map<String, List<String>>> getDisplays() {
        return displays;
    }

    @NotNull
    public Map<String, List<String>> getDisplays(@NotNull CommandSender sender) {
        return getDisplays(getLanguage(sender));
    }

    @NotNull
    public Map<String, List<String>> getDisplays(@NotNull String language) {
        final Map<String, List<String>> map = getDisplaysOrNull(language);
        if (map != null) {
            return map;
        } else if (!language.equals(defaultLanguage)) {
            return displays.getOrDefault(defaultLanguage, Map.of());
        } else {
            return Map.of();
        }
    }

    @Nullable
    public Map<String, List<String>> getDisplaysOrNull(@NotNull CommandSender sender) {
        return getDisplaysOrNull(getLanguage(sender));
    }

    @Nullable
    public Map<String, List<String>> getDisplaysOrNull(@NotNull String language) {
        return displays.get(language);
    }

    @NotNull
    public List<String> getDisplay(@NotNull String path) {
        return getDisplay(getPluginLanguage(), path);
    }

    @NotNull
    public List<String> getDisplay(@NotNull CommandSender sender, @NotNull String path) {
        return getDisplay(getLanguage(sender), path);
    }

    @NotNull
    public List<String> getDisplay(@NotNull String language, @NotNull String path) {
        final List<String> display = getDisplayOrNull(language, path);
        if (display != null) {
            return display;
        } else if (!language.equals(defaultLanguage)) {
            return getDefaultDisplay(path);
        } else {
            return List.of();
        }
    }

    @Nullable
    public List<String> getDisplayOrNull(@NotNull CommandSender sender, @NotNull String path) {
        return getDisplayOrNull(getLanguage(sender), path);
    }

    @Nullable
    public List<String> getDisplayOrNull(@NotNull String language, @NotNull String path) {
        return getDisplays(language).get(path.toLowerCase());
    }

    @NotNull
    public List<String> getDefaultDisplay(@NotNull String path) {
        return getDisplays(defaultLanguage).getOrDefault(path.toLowerCase(), List.of());
    }

    @Nullable
    public List<String> getDefaultDisplayOrNull(@NotNull String path) {
        return getDisplays(defaultLanguage).get(path.toLowerCase());
    }

    @NotNull
    public String getLangText(@NotNull String path) {
        final String text = getLangTextOrNull(path);
        return text == null ? "" : text;
    }

    @NotNull
    public String getLangText(@NotNull CommandSender sender, @NotNull String path) {
        final String text = getLangTextOrNull(sender, path);
        return text == null ? "" : text;
    }

    @Nullable
    public String getLangTextOrNull(@NotNull String path) {
        final List<String> display = getDisplay(getPluginLanguage(), path);
        return display.isEmpty() ? null : display.get(0);
    }

    @Nullable
    public String getLangTextOrNull(@NotNull CommandSender sender, @NotNull String path) {
        final List<String> display = getDisplay(getLanguage(sender), path);
        return display.isEmpty() ? null : display.get(0);
    }

    public void printStackTrace(int level, @NotNull Throwable throwable) {
        if (getLogLevel() >= level) {
            throwable.printStackTrace();
        }
    }

    public void printStackTrace(int level, @NotNull Throwable throwable, @NotNull String msg, @Nullable Object... args) {
        sendLog(level, msg, args);
        printStackTrace(level, throwable);
    }

    public void sendLog(int level, @NotNull String msg, @Nullable Object... args) {
        if (getLogLevel() < level) {
            return;
        }
        for (String s : Strings.replaceArgs(msg, args).split("\n")) {
            switch (level) {
                case 1:
                    plugin.getLogger().severe(s);
                    break;
                case 2:
                    plugin.getLogger().warning(s);
                    break;
                case 3:
                case 4:
                default:
                    plugin.getLogger().info(s);
                    break;
            }
        }
    }

    public void sendTo(@NotNull CommandSender sender, @NotNull String path, @Nullable Object... args) {
        sendTo(sender, getLanguage(sender), path, args);
    }

    protected void sendTo(@NotNull CommandSender sender, @NotNull String language, @NotNull String path, @Nullable Object... args) {
        sendTo(sender, language, path, s -> Strings.replaceArgs(s, args));
    }

    public void sendTo(@NotNull CommandSender sender, @NotNull String path, @NotNull Function<String, String> parser) {
        sendTo(sender, getLanguage(sender), path, parser);
    }

    protected void sendTo(@NotNull CommandSender sender, @NotNull String language, @NotNull String path, @NotNull Function<String, String> parser) {
        for (String s : getDisplay(language, path)) {
            sender.sendMessage(parser.apply(s));
        }
    }

    public void sendToConsole(@NotNull String path, @Nullable Object... args) {
        sendTo(Bukkit.getConsoleSender(), path, args);
    }

    public void sendToConsole(@NotNull String path, @NotNull Function<String, String> parser) {
        sendTo(Bukkit.getConsoleSender(), path, parser);
    }

    public void sendToAll(@NotNull String path, @Nullable Object... args) {
        sendToAll(defaultLanguage, path, args);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @Nullable Object... args) {
        sendToAll(language, path, s -> Strings.replaceArgs(s, args));
    }

    public void sendToAll(@NotNull String path, @NotNull Function<String, String> parser) {
        sendToAll(defaultLanguage, path, parser);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @NotNull Function<String, String> parser) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTo(player, language, path, parser);
        }
    }

    public void sendToAll(@NotNull String path, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> playerParser) {
        sendToAll(defaultLanguage, path, parser, playerParser);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> playerParser) {
        final List<String> display = getDisplay(language, path).stream().map(parser).collect(Collectors.toList());
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String s : display) {
                player.sendMessage(playerParser.apply(player, s));
            }
        }
    }

    public static class Value {

        private final String path;

        private LangLoader loader;

        public Value(@NotNull String path) {
            this.path = path;
        }

        @NotNull
        public String getPath() {
            return path;
        }

        @Nullable
        protected LangLoader getLoader() {
            return loader;
        }

        @NotNull
        public List<String> getDisplay() {
            return loader.getDisplay(path);
        }

        @NotNull
        public List<String> getDisplay(@NotNull CommandSender sender) {
            return loader.getDisplay(sender, path);
        }

        @NotNull
        public String getText() {
            return loader.getLangText(path);
        }

        @NotNull
        public String getText(@NotNull CommandSender sender) {
            return loader.getLangText(sender, path);
        }

        protected void setLoader(@Nullable LangLoader loader) {
            this.loader = loader;
        }

        public void sendTo(@NotNull CommandSender sender, @Nullable Object... args) {
            loader.sendTo(sender, path, args);
        }

        public void sendTo(@NotNull CommandSender sender, @NotNull Function<String, String> parser) {
            loader.sendTo(sender, path, parser);
        }

        public void sendToConsole(@Nullable Object... args) {
            loader.sendToConsole(path, args);
        }

        public void sendToConsole(@NotNull Function<String, String> parser) {
            loader.sendToConsole(path, parser);
        }

        public void sendToAll(@Nullable Object... args) {
            loader.sendToAll(path, args);
        }

        public void sendToAll(@NotNull String language, @Nullable Object... args) {
            loader.sendToAll(language, path, args);
        }

        public void sendToAll(@NotNull Function<String, String> parser) {
            loader.sendToAll(path, parser);
        }

        public void sendToAll(@NotNull String language, @NotNull Function<String, String> parser) {
            loader.sendToAll(language, path, parser);
        }

        public void sendToAll(@NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> playerParser) {
            loader.sendToAll(path, parser, playerParser);
        }

        public void sendToAll(@NotNull String language, @NotNull Function<String, String> parser, @NotNull BiFunction<CommandSender, String, String> playerParser) {
            loader.sendToAll(language, path, parser, playerParser);
        }
    }
}
