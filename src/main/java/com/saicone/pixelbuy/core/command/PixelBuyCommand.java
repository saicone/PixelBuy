package com.saicone.pixelbuy.core.command;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.sub.DatabaseCommand;
import com.saicone.pixelbuy.core.command.sub.ReloadCommand;
import com.saicone.pixelbuy.core.command.sub.PlayerDataCommand;
import com.saicone.pixelbuy.core.command.sub.StoreCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class PixelBuyCommand extends Command {

    private final PixelBuy pl = PixelBuy.get();
    private final String cmd;

    private static final List<SubCommand> subCommands = Arrays.asList(
            new DatabaseCommand(),
            new PlayerDataCommand(),
            new ReloadCommand(),
            new StoreCommand()
    );

    public PixelBuyCommand(String cmd, List<String> aliases) {
        super(cmd);
        this.cmd = this.getName();
        setAliases(aliases);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!hasPerm(sender, pl.getFiles().getConfig().getString("Perms.Main", "pixelbuy.use"))) return true;
        if (args.length == 0) {
            Lang.COMMAND_HELP.sendTo(sender, cmd);
            return true;
        }
        boolean matched = false;
        for (SubCommand sub : subCommands) {
            if (sub.getAliases().matcher(args[0].toLowerCase()).matches()) {
                if (hasPerm(sender, sub.getPermission())) {
                    sub.execute(sender, cmd, args);
                }
                matched = true;
                break;
            }
        }
        if (!matched) {
            Lang.COMMAND_HELP.sendTo(sender, cmd);
        }
        return true;
    }

    public boolean hasPerm(CommandSender sender, String perm) {
        if (sender.hasPermission(perm) || sender.hasPermission(pl.getFiles().getConfig().getString("Perms.All", "pixelbuy.*"))) {
            return true;
        }
        Lang.COMMAND_NO_PERM.sendTo(sender);
        return false;
    }
}