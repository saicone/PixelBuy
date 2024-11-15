package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.command.PixelCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PingCommand extends PixelCommand {

    public PingCommand() {
        super("ping");
    }

    @Override
    public boolean main() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        PixelBuy.get().getDatabase().getMessenger().ping().thenAccept(time -> {
            if (time == Long.MIN_VALUE) {
                sender.sendMessage("Timeout after 20 seconds!");
            } else {
                sender.sendMessage("Ping: " + time + " ms");
            }
        });
    }
}