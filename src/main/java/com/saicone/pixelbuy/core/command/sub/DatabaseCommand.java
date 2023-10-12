package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.SubCommand;

import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class DatabaseCommand extends SubCommand {

    private final PixelBuy pl = PixelBuy.get();

    @Override
    public Pattern getAliases() {
        return Pattern.compile("d(ata)?b(ase)?");
    }

    @Override
    public String getPermission() {
        return PixelBuy.settings().getString("Perms.Database", "pixelbuy.database");
    }

    @Override
    public void execute(CommandSender sender, String cmd, String[] args) {
        if (args.length == 1) {
            Lang.COMMAND_DATABASE_HELP.sendTo(sender, cmd);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "convert":
                if (args.length == 2) {
                    Lang.COMMAND_DATABASE_CONVERT_USAGE.sendTo(sender, cmd);
                } else if (args.length == 3) {
                    pl.getDatabase().convertData(sender, args[2], false);
                } else {
                    pl.getDatabase().convertData(sender, args[2], Boolean.getBoolean(args[3]));
                }
                break;
            case "delete":
                if (args.length == 2) {
                    Lang.COMMAND_DATABASE_DELETE_USAGE.sendTo(sender, cmd);
                } else {
                    pl.getDatabase().deleteData(args[2]);
                    Lang.COMMAND_DATABASE_DELETE_DONE.sendTo(sender, cmd);
                }
                break;
            default:
                Lang.COMMAND_DATABASE_HELP.sendTo(sender, cmd);
                break;
        }
    }
}
