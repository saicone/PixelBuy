package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
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
    public int getMinArgs() {
        return 1;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        final long before = System.currentTimeMillis();
        switch (args[0].toLowerCase()) {
            case "files":
                PixelBuy.settings().loadFrom(PixelBuy.get().getDataFolder(), true);
                PixelBuy.get().getLang().load();
                PixelBuy.get().onReloadSettings();
                break;
            case "store":
                PixelBuy.get().getStore().onLoad();
                break;
            case "database":
                PixelBuy.get().getDatabase().onReload();
                break;
            case "command":
                PixelBuy.get().getCommand().onLoad(PixelBuy.settings());
                break;
            case "all":
                PixelBuy.get().onReload();
                break;
            default:
                sendUsage(sender, cmd, args);
                return;
        }
        final long time = System.currentTimeMillis() - before;
        PixelBuy.get().getLang().sendTo(sender, "Command.Reload." + args[0], time);
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return TYPES;
    }
}
