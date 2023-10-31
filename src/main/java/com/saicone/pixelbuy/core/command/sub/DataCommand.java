package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelCommand;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DataCommand extends PixelCommand {

    public DataCommand() {
        super("data");
    }

    @Override
    public boolean main() {
        return true;
    }

    @Override
    public @NotNull String getUsage(@NotNull CommandSender sender) {
        return Lang.COMMAND_DATABASE_HELP.getText(sender);
    }

    @Override
    public @NotNull String getDescription(@NotNull CommandSender sender) {
        return "Manage data";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {

    }
}
