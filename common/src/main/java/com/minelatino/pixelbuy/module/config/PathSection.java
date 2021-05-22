package com.minelatino.pixelbuy.module.config;

import com.minelatino.pixelbuy.util.PixelUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PathSection {

    private final Settings settings;
    private final String path;
    private final String name;
    private final Map<String, Object> objects;

    public PathSection(Settings settings, String path, String name, Map<String, Object> objects) {
        this.settings = settings;
        this.path = path;
        this.name = name;
        this.objects = objects;
    }

    public Settings getSettings() {
        return settings;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public Object get(@NotNull String path) {
        return get(path, null);
    }

    public Object get(@NotNull String path, Object def) {
        String[] s = path.split("\\.", 2);
        if (s.length == 1) {
            return objects.getOrDefault(path, def);
        } else {
            Object obj = objects.get(s[0]);
            if (obj == null) {
                return def;
            } else if (obj instanceof PathSection) {
                return ((PathSection) obj).get(s[1], def);
            } else {
                return obj;
            }
        }
    }

    public @NotNull String getString(@NotNull String path) {
        return String.valueOf(get(path));
    }

    public @NotNull String getString(@NotNull String path, @NotNull String def) {
        return String.valueOf(get(path, def));
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<String> getStringList(@NotNull String path) {
        Object list = get(path);
        if (list instanceof List) {
            return (List<String>) list;
        } else {
            return new ArrayList<>();
        }
    }

    public int getInt(@NotNull String path) {
        return getInt(path, -1);
    }

    public int getInt(@NotNull String path, int def) {
        return PixelUtils.parseInt(getString(path), def);
    }

    public boolean getBoolean(@NotNull String path) {
        return getBoolean(path, false);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        Object bool = get(path);
        if (bool instanceof Boolean) {
            return (boolean) bool;
        } else {
            return def;
        }
    }

    @Nullable
    public PathSection getSection(@NotNull String path) {
        Object section = get(path);
        if (section instanceof PathSection) {
            return (PathSection) section;
        } else {
            return null;
        }
    }
}
