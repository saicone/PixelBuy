package com.minelatino.pixelbuy.command;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.command.sub.DatabaseCommand;
import com.minelatino.pixelbuy.command.sub.ReloadCommand;
import com.minelatino.pixelbuy.command.sub.PlayerDataCommand;
import com.minelatino.pixelbuy.command.sub.StoreCommand;
import com.minelatino.pixelbuy.util.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class MainCommand extends Command {

    private final PixelBuy pl = PixelBuy.get();
    private final String cmd;

    private static final List<SubCommand> subCommands = Arrays.asList(
            new DatabaseCommand(),
            new PlayerDataCommand(),
            new ReloadCommand(),
            new StoreCommand()
    );

    public MainCommand(String cmd, List<String> aliases) {
        super(cmd);
        this.cmd = this.getName();
        setAliases(aliases);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!hasPerm(sender, pl.getFiles().getConfig().getString("Perms.Main", "pixelbuy.use"))) return true;
        if (args.length == 0) {
            pl.langStringList("Command.Help").forEach(string -> sender.sendMessage(Utils.color(string.replace("%cmd%", cmd))));
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
        if (!matched) pl.langStringList("Command.Help").forEach(string -> sender.sendMessage(Utils.color(string.replace("%cmd%", cmd))));
        return true;
    }

    public boolean hasPerm(CommandSender sender, String perm) {
        if (sender.hasPermission(perm) || sender.hasPermission(pl.getFiles().getConfig().getString("Perms.All", "pixelbuy.*"))) return true;
        sender.sendMessage(Utils.color(pl.langString("Command.No-Perm")));
        return false;
    }
}