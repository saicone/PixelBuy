package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.core.command.PixelCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DataCommand extends PixelCommand {

    // transfer <player> <player>
    // lookup
    //   - player <player>
    //   - order <provider:id:group>

    public DataCommand() {
        super("data");
    }

    @Override
    public boolean main() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {

    }
}
