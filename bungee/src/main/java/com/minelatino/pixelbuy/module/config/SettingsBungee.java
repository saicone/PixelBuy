package com.minelatino.pixelbuy.module.config;

import com.minelatino.pixelbuy.PixelBuyBungee;
import com.minelatino.pixelbuy.util.Files;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsBungee extends Settings {

    private final PixelBuyBungee pl = PixelBuyBungee.get();

    private Configuration config;
    private Configuration defaultConfig;

    public SettingsBungee(String path) {
        super(path);
    }

    public SettingsBungee(String path, boolean requireDefault) {
        super(path, requireDefault);
    }

    public SettingsBungee(String path, String defPath, boolean requireDefault) {
        super(path, defPath, requireDefault);
    }

    @Override
    void load(String defPath, boolean requireDefault) {
        InputStream in = pl.getResourceAsStream(defPath);
        if (in == null) {
            if (requireDefault) {
                pl.getLogger().severe("Cannot find " + defPath + " file on plugin JAR!");
                pl.setRuntimeError(true);
                return;
            }
            defaultExists = false;
        } else {
            defaultConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(in);
            reload();
        }
    }

    @Override
    public void reload() {
        cache.clear();
        sections.clear();
        File folder = pl.getDataFolder();
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(pl.getDataFolder() + File.separator + path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Files.writeStream(file, pl.getResourceAsStream(path));
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            if (!defaultExists) defaultConfig = config;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    Object get(@NotNull String path) {
        return config.get(path, getDefault(path));
    }

    @Override
    Object getDefault(@NotNull String path) {
        return defaultConfig.get(path);
    }

    @Override
    Object get(@NotNull String path, Object def) {
        return config.get(path, def);
    }

    @Override
    boolean isSection(@NotNull Object object) {
        return object instanceof Configuration;
    }

    @Override
    PathSection toSection(@NotNull String path, @NotNull Object object) {
        int index = path.lastIndexOf('.');
        String name = path.substring(index);
        if (name.isEmpty()) return null;

        Map<String, Object> objects = new HashMap<>();
        ((Configuration) object).getKeys().forEach(key -> {
            Object obj = get(path + "." + key);
            if (isSection(obj)) {
                objects.put(key, toSection(path + "." + key, obj));
            } else {
                objects.put(key, obj);
            }
        });

        return new PathSection(this, path.substring(0, index), name, objects);
    }
}
