package com.saicone.pixelbuy.module.settings;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SettingsFile extends BukkitSettings {

    public static final boolean ALLOW_COMMENTS;

    static {
        boolean allowComments = false;
        try {
            FileConfigurationOptions.class.getDeclaredMethod("parseComments");
            allowComments = true;
        } catch (NoSuchMethodException ignored) { }
        ALLOW_COMMENTS = allowComments;
    }

    private final String path;

    public SettingsFile(@NotNull String path) {
        this(path, false);
    }

    public SettingsFile(@NotNull String path, boolean parseComments) {
        this.path = path;
        if (parseComments && ALLOW_COMMENTS) {
            options().parseComments(true);
        }
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void loadFrom(@NotNull File folder) {
        loadFrom(folder, false);
    }

    public void loadFrom(@NotNull File folder, boolean save) {
        if (save) {
            saveResource(folder, path, false);
        }

        final File file = new File(folder, path);
        try {
            load(file);
            if (!save) {
                return;
            }

            final InputStream in = getResource(path);
            if (in == null) {
                return;
            }
            setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(new BufferedInputStream(in), Charsets.UTF_8)));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @NotNull
    public static File getFile(@NotNull String path) {
        return getFile(null, path);
    }

    @NotNull
    public static File getFile(@Nullable File parent, @NotNull String path) {
        File file = parent;
        for (String s : path.split("/")) {
            if (s.isBlank()) {
                continue;
            }
            file = file == null ? new File(s) : new File(file, s);
        }
        if (file == null) {
            throw new IllegalArgumentException("Cannot get file from blank path");
        }
        return file;
    }

    @Nullable
    public static InputStream getResource(@NotNull String name) {
        try {
            final URL url = BukkitSettings.class.getClassLoader().getResource(name);
            if (url == null) {
                return null;
            }

            final URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    public static void saveResource(@NotNull File folder, @NotNull String path, boolean replace) {
        if (path.isBlank()) {
            return;
        }
        path = path.replace('\\', '/');

        final File file = new File(folder, path);
        if (!replace && file.exists()) {
            return;
        }

        final InputStream in = getResource(path);
        if (in == null) {
            return;
        }

        if (!folder.exists()) {
            folder.mkdirs();
        }

        try {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
