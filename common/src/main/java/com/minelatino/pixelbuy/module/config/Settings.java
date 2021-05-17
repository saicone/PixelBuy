package com.minelatino.pixelbuy.module.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Settings {

    final Map<String, Object> cache = new HashMap<>();
    final Map<String, PathSection> sections = new HashMap<>();

    String path;
    boolean defaultExists = true;

    public Settings(String path) {
        this(path, true);
    }

    public Settings(String path, boolean requireDefault) {
        this(path, path, requireDefault);
    }

    public Settings(String path, String defPath, boolean requireDefault) {
        this.path = path;
        load(defPath, requireDefault);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    abstract void load(String defPath, boolean requireDefault);

    public abstract void reload();

    abstract Object get(@NotNull String path);

    abstract Object getDefault(@NotNull String path);

    abstract Object get(@NotNull String path, Object def);

    abstract PathSection getSection0(@NotNull String path);

    public @NotNull String getString(@NotNull String path) {
        return String.valueOf(cache.getOrDefault(path, cache(path, get(path))));
    }

    public @NotNull String getString(@NotNull String path, String def) {
        return String.valueOf(cache.getOrDefault(path, def));
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<String> getStringList(@NotNull String path) {
        return (List<String>) cache.getOrDefault(path, getStringList0(path));
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringList0(String path) {
        final Object object = get(path);
        if (object instanceof List) {
            return (List<String>) cache(path, object);
        }
        return Collections.singletonList(String.valueOf(cache(path, object)));
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<Object> getList(@NotNull String path) {
        return (List<Object>) cache.getOrDefault(path, getList0(path));
    }

    @SuppressWarnings("unchecked")
    private List<Object> getList0(String path) {
        final Object object = get(path);
        if (object instanceof List) {
            return (List<Object>) cache(path, object);
        }
        return Collections.singletonList(cache(path, object));
    }

    public int getInt(@NotNull String path) {
        return (int) cache.getOrDefault(path, getInt0(path));
    }

    private int getInt0(String path) {
        Object object = get(path);
        if (object instanceof Integer) {
            return (int) cache(path, object);
        }
        try {
            return Integer.parseInt(String.valueOf(cache(path, object)));
        } catch (NumberFormatException e) {
            // Debug: Path require a integer, will use default config in this case
            e.printStackTrace();
            return (int) cache(path, getDefault(path));
        }
    }

    public int getInt(@NotNull String path, int def) {
        return (int) cache.getOrDefault(path, def);
    }

    public boolean getBoolean(@NotNull String path) {
        return (boolean) cache.getOrDefault(path, getBoolean0(path));
    }

    private boolean getBoolean0(String path) {
        Object object = get(path);
        if (object instanceof Boolean) {
            return (boolean) cache(path, object);
        }
        // Debug: Path require a boolean, will use default config in this case
        return (boolean) cache(path, getDefault(path));
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        return (boolean) cache.getOrDefault(path, def);
    }

    @Nullable
    public PathSection getSection(@NotNull String path) {
        return sections.getOrDefault(path, getSection0(path));
    }

    private Object cache(String path, Object obj) {
        cache.put(path, obj);
        return obj;
    }
}
