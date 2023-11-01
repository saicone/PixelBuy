package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.core.command.PixelCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StoreCommand extends PixelCommand {

    public StoreCommand() {
        super("store");
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

    }
}
