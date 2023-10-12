package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.SubCommand;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class DatabaseCommand extends SubCommand {

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
        switch (args[1].toLowerCase()) {
            case "convert":
                if (args.length == 2) {
                    Lang.COMMAND_DATABASE_CONVERT_USAGE.sendTo(sender, cmd);
                } else if (args.length == 3) {
                    plugin.getDatabase().convertData(sender, args[2], false);
                } else {
                    plugin.getDatabase().convertData(sender, args[2], Boolean.getBoolean(args[3]));
                }
                break;
            case "delete":
                if (args.length == 2) {
                    Lang.COMMAND_DATABASE_DELETE_USAGE.sendTo(sender, cmd);
                } else {
                    plugin.getDatabase().deleteData(args[2]);
                    Lang.COMMAND_DATABASE_DELETE_DONE.sendTo(sender, cmd);
                }
                break;
            default:
                Lang.COMMAND_DATABASE_HELP.sendTo(sender, cmd);
                break;
        }
    }
}
