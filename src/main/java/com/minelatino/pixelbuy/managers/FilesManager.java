package com.minelatino.pixelbuy.managers;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class FilesManager {

    private final Plugin pl = PixelBuy.get();
    private final File langFolder = new File(pl.getDataFolder() + File.separator + "lang");

    private YamlConfiguration settings;
    private YamlConfiguration messages;

    public FilesManager(CommandSender sender) {
        reloadSettings(sender);
        reloadLang(sender);
    }

    public void reloadSettings(CommandSender sender) {
        File cF = new File(pl.getDataFolder(), "settings.yml");
        if (!cF.exists()) pl.saveResource("settings.yml", false);
        settings = YamlConfiguration.loadConfiguration(cF);
    }

    public void reloadLang(CommandSender sender) {
        if (!langFolder.exists()) {
            pl.saveResource("lang/en.yml", false);
            pl.saveResource("lang/es.yml", false);
        }
        String lang = getSettings().getString("Language", "en");
        File cF = new File(langFolder, lang + ".yml");
        if (cF.exists()) {
            messages = YamlConfiguration.loadConfiguration(cF);
            sender.sendMessage(Utils.color(messages.getString("Command.Reload.Messages.Success")));
        } else if (!cF.exists() && (lang.equals("en") || lang.equals("es"))) {
            pl.saveResource("lang/" + lang + ".yml", false);
            cF = new File(langFolder, lang + ".yml");
            messages = YamlConfiguration.loadConfiguration(cF);
            sender.sendMessage(Utils.color(messages.getString("Command.Reload.Messages.Saved")));
        } else {
            if (!new File(langFolder, "en.yml").exists()) pl.saveResource("lang/en.yml", false);
            messages = YamlConfiguration.loadConfiguration(new File(langFolder, "en.yml"));
            sender.sendMessage(Utils.color(messages.getString("Command.Reload.Messages.Error")));
        }
    }

    public YamlConfiguration getSettings() {
        return settings;
    }

    public YamlConfiguration getMessages() {
        return messages;
    }
}
