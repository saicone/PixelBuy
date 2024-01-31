package com.saicone.pixelbuy.core.command;

import com.saicone.pixelbuy.core.command.sub.*;

public class PixelBuyCommand extends PixelCommand {

    public PixelBuyCommand() {
        super("pixelbuy", new TestCommand(), new ConversionCommand(), new DataCommand(), new OrderCommand(), new ReloadCommand(), new StoreCommand(), new UserCommand());
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