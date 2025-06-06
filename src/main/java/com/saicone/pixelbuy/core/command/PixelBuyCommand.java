package com.saicone.pixelbuy.core.command;

import com.saicone.pixelbuy.core.command.sub.DataCommand;
import com.saicone.pixelbuy.core.command.sub.GiveCommand;
import com.saicone.pixelbuy.core.command.sub.OrderCommand;
import com.saicone.pixelbuy.core.command.sub.PingCommand;
import com.saicone.pixelbuy.core.command.sub.ReloadCommand;
import com.saicone.pixelbuy.core.command.sub.StoreCommand;
import com.saicone.pixelbuy.core.command.sub.UserCommand;

public class PixelBuyCommand extends PixelCommand {

    public PixelBuyCommand() {
        super("pixelbuy", new PingCommand(), new DataCommand(), new OrderCommand(), new ReloadCommand(), new StoreCommand(), new UserCommand(), new GiveCommand());
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public boolean main() {
        return true;
    }
}