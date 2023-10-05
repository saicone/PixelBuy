package com.saicone.pixelbuy.managers;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.util.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class FilesManager {

    private final PixelBuy pl = PixelBuy.get();
    private final File langFolder = new File(pl.getDataFolder() + File.separator + "lang");

    private FileConfiguration settings;
    private FileConfiguration messages;

    public FilesManager(CommandSender sender) {
        reloadSettings(sender, true);
        reloadLang(sender, getConfig().getString("Language", "en"));
    }

    public void reloadSettings(CommandSender sender, boolean init) {
        File cF = new File(pl.getDataFolder(), "settings.yml");
        if (!cF.exists()) pl.saveResource("settings.yml", false);
        settings = YamlConfiguration.loadConfiguration(cF);
        if (!init) sender.sendMessage(Utils.color(getLang().getString("Command.Reload.Files.Settings")));
    }

    public void reloadLang(CommandSender sender, String lang) {
        if (!langFolder.exists()) {
            pl.saveResource("lang/en.yml", false);
            pl.saveResource("lang/es.yml", false);
        }
        File cF = new File(langFolder, lang + ".yml");
        if (cF.exists()) {
            messages = YamlConfiguration.loadConfiguration(cF);
            sender.sendMessage(Utils.color(getLang().getString("Command.Reload.Files.Messages.Success")));
        } else if (!cF.exists() && (lang.equals("en") || lang.equals("es"))) {
            pl.saveResource("lang/" + lang + ".yml", false);
            cF = new File(langFolder, lang + ".yml");
            messages = YamlConfiguration.loadConfiguration(cF);
            sender.sendMessage(Utils.color(getLang().getString("Command.Reload.Files.Messages.Saved")));
        } else {
            if (!new File(langFolder, "en.yml").exists()) pl.saveResource("lang/en.yml", false);
            messages = YamlConfiguration.loadConfiguration(new File(langFolder, "en.yml"));
            sender.sendMessage(Utils.color(getLang().getString("Command.Reload.Files.Messages.Error")));
        }
    }

    public FileConfiguration getConfig() {
        return settings;
    }

    public FileConfiguration getLang() {
        return messages;
    }
}
