package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelCommand;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReloadCommand extends PixelCommand {

    private static final List<String> TYPES = List.of("files", "store", "database", "command", "all");

    public ReloadCommand() {
        super("reload");
    }

    @Override
    public boolean main() {
        return true;
    }

    @Override
    public @NotNull String getUsage(@NotNull CommandSender sender) {
        return Lang.COMMAND_RELOAD_HELP.getText(sender);
    }

    @Override
    public @NotNull String getDescription(@NotNull CommandSender sender) {
        return "Reload plugin";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        switch (args[1].toLowerCase()) {
            case "file":
            case "files":
                PixelBuy.settings().loadFrom(PixelBuy.get().getDataFolder(), true);
                PixelBuy.get().getLang().load();
                Lang.COMMAND_RELOAD_FILES.sendTo(sender);
                break;
            case "store":
                PixelBuy.get().getStore().onLoad();
                break;
            case "database":
                PixelBuy.get().getDatabase().onReload();
                break;
            case "command":
                PixelBuy.get().getCommand().onLoad(PixelBuy.settings());
                Lang.COMMAND_RELOAD_COMMAND.sendTo(sender);
                break;
            case "all":
                PixelBuy.get().onReload();
                Lang.COMMAND_RELOAD_WEBDATA.sendTo(sender);
                break;
            default:
                sendUsage(sender, cmd, args);
                break;
        }
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return TYPES;
    }
}
