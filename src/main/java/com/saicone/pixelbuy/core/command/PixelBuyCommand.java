package com.saicone.pixelbuy.core.command;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.sub.DataCommand;
import com.saicone.pixelbuy.core.command.sub.ReloadCommand;
import com.saicone.pixelbuy.core.command.sub.UserCommand;
import com.saicone.pixelbuy.core.command.sub.StoreCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PixelBuyCommand extends Command {

    private static final List<SubCommand> SUB_COMMANDS = Arrays.asList(
            new DataCommand(),
            new UserCommand(),
            new ReloadCommand(),
            new StoreCommand()
    );

    private final PixelBuy plugin = PixelBuy.get();

    public PixelBuyCommand(@NotNull String cmd, @NotNull List<String> aliases) {
        super(cmd);
        setAliases(aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!hasPerm(sender, PixelBuy.settings().getString("Perms.Main", "pixelbuy.use"))) return true;
        if (args.length == 0) {
            Lang.COMMAND_HELP.sendTo(sender, getName());
            return true;
        }
        boolean matched = false;
        for (SubCommand sub : SUB_COMMANDS) {
            if (sub.getAliases().matcher(args[0].toLowerCase()).matches()) {
                if (hasPerm(sender, sub.getPermission())) {
                    sub.execute(sender, getName(), args);
                }
                matched = true;
                break;
            }
        }
        if (!matched) {
            Lang.COMMAND_HELP.sendTo(sender, getName());
        }
        return true;
    }

    public boolean hasPerm(@NotNull CommandSender sender, @NotNull String perm) {
        if (sender.hasPermission(perm) || sender.hasPermission(PixelBuy.settings().getString("Perms.All", "pixelbuy.*"))) {
            return true;
        }
        Lang.COMMAND_NO_PERM.sendTo(sender);
        return false;
    }
}