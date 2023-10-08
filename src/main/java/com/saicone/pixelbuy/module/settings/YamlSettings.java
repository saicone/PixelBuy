package com.saicone.pixelbuy.module.settings;

import com.saicone.pixelbuy.PixelBuy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class YamlSettings {

    private final PixelBuy pl = PixelBuy.get();

    private FileConfiguration settings;

    public YamlSettings() {
        reloadSettings();
    }

    public void reloadSettings() {
        File cF = new File(pl.getDataFolder(), "settings.yml");
        if (!cF.exists()) pl.saveResource("settings.yml", false);
        settings = YamlConfiguration.loadConfiguration(cF);
    }

    public FileConfiguration getConfig() {
        return settings;
    }

}
