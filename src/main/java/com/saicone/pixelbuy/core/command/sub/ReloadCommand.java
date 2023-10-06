package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.command.SubCommand;
import com.saicone.pixelbuy.util.Utils;

import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class ReloadCommand extends SubCommand {

    private final PixelBuy pl = PixelBuy.get();

    @Override
    public Pattern getAliases() {
        return Pattern.compile("re(load|fresh)?");
    }

    @Override
    public String getPermission() {
        return pl.getFiles().getConfig().getString("Perms.Reload", "pixelbuy.reload");
    }

    @Override
    public void execute(CommandSender sender, String cmd, String[] args) {
        if (args.length == 1) {
            pl.langStringList("Command.Reload.Help").forEach(string -> sender.sendMessage(Utils.color(string.replace("%cmd%", cmd))));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "file":
            case "files":
                if (args.length == 3) {
                    if (args[2].toLowerCase().equals("settings")) {
                        pl.getFiles().reloadSettings(sender, false);
                        break;
                    }
                    if (args[2].toLowerCase().equals("messages")) {
                        pl.getFiles().reloadLang(sender, pl.getFiles().getConfig().getString("Language"));
                        break;
                    }
                    sender.sendMessage(Utils.color(pl.langString("Command.Reload.Files.Use").replace("%cmd%", cmd)));
                    break;
                }
                pl.getFiles().reloadSettings(sender, false);
                pl.getFiles().reloadLang(sender, pl.getFiles().getConfig().getString("Language"));
                break;
            case "store":
                pl.getStore().reload(sender, false);
                break;
            case "database":
                pl.getDatabase().reload(sender);
                break;
            case "webdata":
                pl.getOrderManager().reload(false);
                sender.sendMessage(Utils.color(pl.langString("Command.Reload.Webdata.Success")));
                break;
            case "command":
                pl.reloadCommand();
                sender.sendMessage(Utils.color(pl.langString("Command.Reload.Command")));
                break;
            case "all":
                pl.getFiles().reloadSettings(sender, false);
                pl.getFiles().reloadLang(sender, pl.getFiles().getConfig().getString("Language"));
                pl.getStore().reload(sender, false);
                pl.getDatabase().reload(sender);
                pl.getOrderManager().reload(false);
                sender.sendMessage(Utils.color(pl.langString("Command.Reload.Webdata.Success")));
                break;
            default:
                pl.langStringList("Command.Reload.Help").forEach(string -> sender.sendMessage(Utils.color(string.replace("%cmd%", cmd))));
        }
    }
}
