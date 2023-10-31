package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.core.command.PixelCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class OrderCommand extends PixelCommand {

    public OrderCommand() {
        super("order");
    }

    @Override
    public boolean main() {
        return true;
    }

    @Override
    public @NotNull String getUsage(@NotNull CommandSender sender) {
        return "";
    }

    @Override
    public @NotNull String getDescription(@NotNull CommandSender sender) {
        return "Manage order";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {

    }
}