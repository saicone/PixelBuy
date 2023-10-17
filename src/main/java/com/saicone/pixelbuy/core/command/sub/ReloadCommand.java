package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.SubCommand;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class ReloadCommand extends SubCommand {

    private final PixelBuy plugin = PixelBuy.get();

    @Override
    public @NotNull Pattern getAliases() {
        return Pattern.compile("re(load|fresh)?");
    }

    @Override
    public @NotNull String getPermission() {
        return PixelBuy.settings().getString("Perms.Reload", "pixelbuy.reload");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String cmd, @NotNull String[] args) {
        if (args.length == 1) {
            Lang.COMMAND_RELOAD_HELP.sendTo(sender, cmd);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "file":
            case "files":
                plugin.getSettings().loadFrom(plugin.getDataFolder(), true);
                plugin.getLang().load();
                Lang.COMMAND_RELOAD_FILES.sendTo(sender);
                break;
            case "store":
                plugin.getStore().onLoad();
                break;
            case "database":
                plugin.getDatabase().reload(sender);
                break;
            case "command":
                plugin.reloadCommand();
                Lang.COMMAND_RELOAD_COMMAND.sendTo(sender);
                break;
            case "all":
                plugin.onReload();
                plugin.getDatabase().reload(sender);
                Lang.COMMAND_RELOAD_WEBDATA.sendTo(sender);
                break;
            default:
                Lang.COMMAND_RELOAD_HELP.sendTo(sender, cmd);
                break;
        }
    }
}
