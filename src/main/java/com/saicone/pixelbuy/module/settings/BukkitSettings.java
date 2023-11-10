package com.saicone.pixelbuy.module.settings;

import com.saicone.pixelbuy.util.OptionalType;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class BukkitSettings extends YamlConfiguration {

    private static final MethodHandle MAP;

    static {
        MethodHandle map = null;
        try {
            final var field = MemorySection.class.getDeclaredField("map");
            field.setAccessible(true);
            map = MethodHandles.lookup().unreflectGetter(field);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        MAP = map;
    }

    private final ConfigurationSection delegate;

    @Nullable
    @Contract("!null -> !null")
    public static BukkitSettings of(@Nullable Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof BukkitSettings) {
            return (BukkitSettings) object;
        } else if (object instanceof ConfigurationSection) {
            return new BukkitSettings((ConfigurationSection) object);
        } else if (object instanceof Map) {
            final BukkitSettings settings = new BukkitSettings();
            settings.set((Map<?, ?>) object);
            return settings;
        } else {
            throw new IllegalArgumentException("The object type '" + object.getClass().getName() + "' cannot be converted to BukkitSettings instance");
        }
    }

    public BukkitSettings() {
        this(null);
    }

    public BukkitSettings(@Nullable ConfigurationSection delegate) {
        this.delegate = delegate;
    }

    @NotNull
    public ConfigurationSection getDelegate() {
        return delegate;
    }

    @NotNull
    @Override
    public Set<String> getKeys(boolean deep) {
        return delegate == null ? super.getKeys(deep) : delegate.getKeys(deep);
    }

    @NotNull
    @Override
    public Map<String, Object> getValues(boolean deep) {
        return delegate == null ? super.getValues(deep) : delegate.getValues(deep);
    }

    @Nullable
    @Override
    public Configuration getRoot() {
        return delegate == null ? super.getRoot() : delegate.getRoot();
    }

    @Nullable
    @Override
    public ConfigurationSection getParent() {
        return delegate == null ? super.getParent() : delegate.getParent();
    }

    @NotNull
    @Override
    public String getName() {
        return delegate == null ? super.getName() : delegate.getName();
    }

    @NotNull
    @Override
    public String getCurrentPath() {
        return delegate == null ? super.getCurrentPath() : delegate.getCurrentPath();
    }

    @Nullable
    @Override
    public Configuration getDefaults() {
        if (delegate instanceof MemoryConfiguration) {
            return ((MemoryConfiguration) delegate).getDefaults();
        }
        return super.getDefaults();
    }

    @Nullable
    @Override
    public Object get(@NotNull String path, @Nullable Object def) {
        return delegate == null ? super.get(path, def) : delegate.get(path, def);
    }

    @Nullable
    public Object get(@NotNull String... path) {
        return getIf(String::equals, path);
    }

    @Nullable
    public Object get(@NotNull Function<BukkitSettings, Object> getter) {
        final Object object = getter.apply(this);
        return object instanceof OptionalType ? ((OptionalType) object).getValue() : object;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected Object getIf(@NotNull Predicate<String> condition) {
        try {
            for (var entry : ((Map<String, ?>) MAP.invoke(getMemorySection())).entrySet()) {
                if (condition.test(entry.getKey())) {
                    return get(entry.getKey());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @Nullable
    protected Object getIf(@NotNull BiPredicate<String, String> condition, @NotNull String... path) {
        Object object = getMemorySection();
        for (String key : path) {
            if (object instanceof MemorySection) {
                object = getIfType((MemorySection) object, condition, key);
                continue;
            }
            return null;
        }
        return object;
    }

    @Nullable
    protected <T> Object getIf(@NotNull Function<String, T> keyConversion, @NotNull BiPredicate<String, T> condition, @NotNull String... path) {
        Object object = getMemorySection();
        for (String key : path) {
            if (object instanceof MemorySection) {
                object = getIfType((MemorySection) object, condition, keyConversion.apply(key));
                continue;
            }
            return null;
        }
        return object;
    }

    @Nullable
    protected <T> Object getIfType(@NotNull BiPredicate<String, T> condition, @NotNull T type) {
        return getIfType(getMemorySection(), condition, type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> Object getIfType(@NotNull MemorySection section, @NotNull BiPredicate<String, T> condition, @NotNull T type) {
        try {
            for (var entry : ((Map<String, ?>) MAP.invoke(section)).entrySet()) {
                if (condition.test(entry.getKey(), type)) {
                    return section.get(entry.getKey());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @NotNull
    public MemorySection getMemorySection() {
        return delegate instanceof MemorySection ? (MemorySection) delegate : this;
    }

    @NotNull
    public ConfigurationSection getConfigurationSection() {
        return delegate == null ? this : delegate;
    }

    @Nullable
    @Override
    public BukkitSettings getConfigurationSection(@NotNull String path) {
        return of(super.getConfigurationSection(path));
    }

    @Nullable
    public BukkitSettings getConfigurationSection(@NotNull Function<BukkitSettings, Object> getter) {
        final Object object = get(getter);
        return object instanceof ConfigurationSection ? of(object) : null;
    }

    @Nullable
    public <T extends ConfigurationSection> T getConfigurationSection(@NotNull String path, @NotNull Function<ConfigurationSection, T> function) {
        final ConfigurationSection section = super.getConfigurationSection(path);
        return section == null ? null : function.apply(section);
    }

    @Nullable
    public <T extends ConfigurationSection> T getConfigurationSection(@NotNull Function<BukkitSettings, Object> getter, @NotNull Function<ConfigurationSection, T> function) {
        final Object object = get(getter);
        return object instanceof ConfigurationSection ? function.apply((ConfigurationSection) object) : null;
    }

    @NotNull
    public List<String> getComments(@NotNull String path) {
        return delegate == null ? super.getComments(path) : delegate.getComments(path);
    }

    @NotNull
    public List<String> getInlineComments(@NotNull String path) {
        return delegate == null ? super.getInlineComments(path) : delegate.getInlineComments(path);
    }

    @NotNull
    public OptionalType getAny(@NotNull String key) {
        return OptionalType.of(get(key));
    }

    @NotNull
    public OptionalType getAny(@NotNull String... path) {
        return OptionalType.of(get(path));
    }

    @NotNull
    public OptionalType getIgnoreCase(@NotNull String key) {
        return OptionalType.of(getIf(s -> s.equalsIgnoreCase(key)));
    }

    @NotNull
    public OptionalType getIgnoreCase(@NotNull String... path) {
        return OptionalType.of(getIf(String::equalsIgnoreCase, path));
    }

    @NotNull
    public OptionalType getRegex(@NotNull @Language("RegExp") String regex) {
        final Pattern pattern = Pattern.compile(regex);
        return OptionalType.of(getIf(s -> pattern.matcher(s).matches()));
    }

    @NotNull
    public OptionalType getRegex(@NotNull @Language("RegExp") String... regexPath) {
        return OptionalType.of(getIf(Pattern::compile, (s, pattern) -> pattern.matcher(s).matches(), regexPath));
    }

    @Nullable
    public SettingsItem getItem(@NotNull String path) {
        return getConfigurationSection(path, SettingsItem::of);
    }

    @Nullable
    public SettingsItem getItem(@NotNull Function<BukkitSettings, Object> getter) {
        return getConfigurationSection(getter, SettingsItem::of);
    }

    public void set(@NotNull ConfigurationSection section) {
        set(section, true);
    }

    public void set(@NotNull ConfigurationSection section, boolean copy) {
        for (String path : section.getKeys(true)) {
            final Object value = section.get(path);
            if (copy && value instanceof List) {
                set(path, new ArrayList<>((List<?>) value));
            } else {
                set(path, value);
            }
        }
    }

    public void set(@NotNull Map<?, ?> map) {
        set(map, true);
    }

    public void set(@NotNull Map<?, ?> map, boolean copy) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = String.valueOf(entry.getKey());
            final Object value = entry.getValue();
            if (value instanceof Map) {
                createSection(key, (Map<?, ?>) value);
            } else if (copy && value instanceof List) {
                set(key, new ArrayList<>((List<?>) value));
            } else {
                set(key, value);
            }
        }
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        if (delegate == null) {
            super.set(path, value);
        } else {
            delegate.set(path, value);
        }
    }

    public void setComments(@NotNull String path, @Nullable List<String> comments) {
        if (delegate == null) {
            super.setComments(path, comments);
        } else {
            delegate.setComments(path, comments);
        }
    }

    public void setInlineComments(@NotNull String path, @Nullable List<String> comments) {
        if (delegate == null) {
            super.setInlineComments(path, comments);
        } else {
            delegate.setInlineComments(path, comments);
        }
    }

    public void merge(@NotNull ConfigurationSection section) {
        merge(section, getConfigurationSection());
    }

    @SuppressWarnings("unchecked")
    public static void merge(@NotNull ConfigurationSection from, @NotNull ConfigurationSection to) {
        for (String key : from.getKeys(false)) {
            final Object value = from.get(key);
            if (to.contains(key)) {
                final Object currentValue = to.get(key);
                if (currentValue instanceof ConfigurationSection) {
                    if (value instanceof ConfigurationSection) {
                        merge((ConfigurationSection) value, (ConfigurationSection) currentValue);
                    }
                } else if (currentValue instanceof List) {
                    for (Object o : OptionalType.of(value)) {
                        try {
                            ((List<Object>) currentValue).add(o);
                        } catch (Throwable ignored) { }
                    }
                }
                continue;
            }
            if (value instanceof ConfigurationSection) {
                merge((ConfigurationSection) value, to.createSection(key));
            } else if (value instanceof List) {
                to.set(key, new ArrayList<>((List<?>) value));
            } else {
                to.set(key, value);
            }
        }
    }

    @NotNull
    @Contract("_ -> new")
    public BukkitSettings parse(@NotNull Function<String, String> function) {
        return parse(getConfigurationSection(), new BukkitSettings(), function);
    }

    @NotNull
    @Contract("_ -> new")
    public BukkitSettings parse(@NotNull BiFunction<String, String, String> function) {
        return parse(getConfigurationSection(), new BukkitSettings(), function);
    }

    @NotNull
    public static <T extends ConfigurationSection> T parse(@NotNull ConfigurationSection from, @NotNull T to, @NotNull Function<String, String> function) {
        return parse(from, to, (path, s) -> function.apply(s));
    }

    @NotNull
    public static <T extends ConfigurationSection> T parse(@NotNull ConfigurationSection from, @NotNull T to, @NotNull BiFunction<String, String, String> function) {
        for (String path : from.getKeys(true)) {
            to.set(path, parse(path, from.get(path), function));
        }
        return to;
    }

    @Nullable
    private static Object parse(@NotNull String path, @Nullable Object object, @NotNull BiFunction<String, String, String> function) {
        if (object instanceof String) {
            return function.apply(path, (String) object);
        } else if (object instanceof List) {
            final List<Object> list = new ArrayList<>();
            int i = 0;
            for (Object o : OptionalType.of(object)) {
                list.add(parse(path + "[" + i + "]", o, function));
                i++;
            }
            return list;
        } else if (object instanceof Map) {
            final Map<String, Object> map = new HashMap<>();
            for (var entry : ((Map<?, ?>) object).entrySet()) {
                final String key = String.valueOf(entry.getKey());
                map.put(key, parse(path + "." + key, entry.getValue(), function));
            }
            return map;
        } else {
            return object;
        }
    }

    @NotNull
    public Map<String, Object> asMap() {
        return asMap(getConfigurationSection());
    }

    @NotNull
    public static Map<String, Object> asMap(@NotNull ConfigurationSection section) {
        final Map<String, Object> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            final Object value = section.get(key);
            if (value instanceof ConfigurationSection) {
                map.put(key, asMap((ConfigurationSection) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }
}
