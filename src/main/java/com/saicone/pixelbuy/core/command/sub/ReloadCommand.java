package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.SubCommand;

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
        return PixelBuy.settings().getString("Perms.Reload", "pixelbuy.reload");
    }

    @Override
    public void execute(CommandSender sender, String cmd, String[] args) {
        if (args.length == 1) {
            Lang.COMMAND_RELOAD_HELP.sendTo(sender, cmd);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "file":
            case "files":
                pl.getSettings().loadFrom(pl.getDataFolder(), true);
                Lang.COMMAND_RELOAD_FILES.sendTo(sender);
                break;
            case "store":
                pl.getStore().reload(sender, false);
                break;
            case "database":
                pl.getDatabase().reload(sender);
                break;
            case "webdata":
                pl.getOrderManager().reload(false);
                Lang.COMMAND_RELOAD_WEBDATA.sendTo(sender);
                break;
            case "command":
                pl.reloadCommand();
                Lang.COMMAND_RELOAD_COMMAND.sendTo(sender);
                break;
            case "all":
                pl.getSettings().loadFrom(pl.getDataFolder(), true);
                pl.getStore().reload(sender, false);
                pl.getDatabase().reload(sender);
                pl.getOrderManager().reload(false);
                Lang.COMMAND_RELOAD_WEBDATA.sendTo(sender);
                break;
            default:
                Lang.COMMAND_RELOAD_HELP.sendTo(sender, cmd);
                break;
        }
    }
}
