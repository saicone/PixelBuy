package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.SubCommand;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class DataCommand extends SubCommand {

    private final PixelBuy plugin = PixelBuy.get();

    @Override
    public @NotNull Pattern getAliases() {
        return Pattern.compile("d(ata)?b(ase)?");
    }

    @Override
    public @NotNull String getPermission() {
        return PixelBuy.settings().getString("Perms.Database", "pixelbuy.database");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String cmd, @NotNull String[] args) {
        if (args.length == 1) {
            Lang.COMMAND_DATABASE_HELP.sendTo(sender, cmd);
            return;
        }

        Lang.COMMAND_DATABASE_HELP.sendTo(sender, cmd);
    }
}
