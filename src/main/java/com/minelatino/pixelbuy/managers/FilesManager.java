package com.minelatino.pixelbuy.managers;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class FilesManager {

    private final PixelBuy pl = PixelBuy.get();
    private final File langFolder = new File(pl.getDataFolder() + File.separator + "lang");

    public FilesManager(CommandSender sender) {
        reloadLang(sender);
    }

    public void reloadSettings(CommandSender sender, boolean init) {
        File cF = new File(pl.getDataFolder(), "settings.yml");
        if (!cF.exists()) pl.saveResource("settings.yml", false);
        pl.setConfig(YamlConfiguration.loadConfiguration(cF));
        if (!init) sender.sendMessage(Utils.color(pl.LANG.getString("Command.Reload.Files.Settings")));
    }

    public void reloadLang(CommandSender sender) {
        if (!langFolder.exists()) {
            pl.saveResource("lang/en.yml", false);
            pl.saveResource("lang/es.yml", false);
        }
        if (pl.SETTINGS == null) reloadSettings(sender, true);
        String lang = pl.SETTINGS.getString("Language", "en");
        File cF = new File(langFolder, lang + ".yml");
        if (cF.exists()) {
            pl.setLang(YamlConfiguration.loadConfiguration(cF));
            sender.sendMessage(Utils.color(pl.LANG.getString("Command.Reload.Files.Messages.Success")));
        } else if (!cF.exists() && (lang.equals("en") || lang.equals("es"))) {
            pl.saveResource("lang/" + lang + ".yml", false);
            cF = new File(langFolder, lang + ".yml");
            pl.setLang(YamlConfiguration.loadConfiguration(cF));
            sender.sendMessage(Utils.color(pl.LANG.getString("Command.Reload.Files.Messages.Saved")));
        } else {
            if (!new File(langFolder, "en.yml").exists()) pl.saveResource("lang/en.yml", false);
            pl.setLang(YamlConfiguration.loadConfiguration(new File(langFolder, "en.yml")));
            sender.sendMessage(Utils.color(pl.LANG.getString("Command.Reload.Files.Messages.Error")));
        }
    }
}
