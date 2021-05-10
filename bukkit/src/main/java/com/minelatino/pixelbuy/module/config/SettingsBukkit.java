package com.minelatino.pixelbuy.module.config;

import com.minelatino.pixelbuy.PixelBuyBukkit;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsBukkit extends Settings {

    private final PixelBuyBukkit pl = PixelBuyBukkit.get();

    private FileConfiguration config;
    private FileConfiguration defaultConfig;

    public SettingsBukkit(String path) {
        super(path);
    }

    public SettingsBukkit(String path, boolean requireDefault) {
        super(path, requireDefault);
    }

    public SettingsBukkit(String path, String defPath, boolean requireDefault) {
        super(path, defPath, requireDefault);
    }

    @Override
    void load(String defPath, boolean requireDefault) {
        InputStream in = pl.getResource(defPath);
        if (in == null) {
            if (requireDefault) {
                Bukkit.getLogger().severe("Cannot find " + defPath + " file on plugin JAR!");
                pl.getPluginLoader().disablePlugin(pl);
                return;
            }
            defaultExists = false;
        } else {
            defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
            reload();
        }
    }

    @Override
    public void reload() {
        getCache().clear();
        String path = pl.getDataFolder() + File.separator + this.path;
        File file = new File(path);
        if (file.exists()) {
            // TODO: Update configuration
        } else {
            pl.saveResource(this.path, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        if (!defaultExists) defaultConfig = config;
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
}
