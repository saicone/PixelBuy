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
import java.util.HashMap;
import java.util.Map;
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
    public static BukkitSettings of(@Nullable ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        if (section instanceof BukkitSettings) {
            return (BukkitSettings) section;
        }
        return new BukkitSettings(section);
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
    @SuppressWarnings("unchecked")
    protected Object getIf(@NotNull Predicate<String> condition) {
        try {
            for (var entry : ((Map<String, ?>) MAP.invoke(this)).entrySet()) {
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
        Object object = this;
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
        Object object = this;
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
        return getIfType(this, condition, type);
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
    public ConfigurationSection getConfigurationSection() {
        return delegate == null ? this : delegate;
    }

    @Nullable
    @Override
    public BukkitSettings getConfigurationSection(@NotNull String path) {
        return of(super.getConfigurationSection(path));
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

    public void set(@NotNull ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            set(key, section.get(key));
        }
    }

    public void set(@NotNull Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof Map) {
                createSection(entry.getKey(), (Map<?, ?>) value);
            } else {
                set(entry.getKey(), value);
            }
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
