package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.PixelCommand;
import com.saicone.pixelbuy.core.store.StoreItem;
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
    public @NotNull String getUsage(@NotNull CommandSender sender) {
        return Lang.COMMAND_STORE_HELP.getText(sender);
    }

    @Override
    public @NotNull String getDescription(@NotNull CommandSender sender) {
        return "Manage store";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        int itemNum = 1;
        for (var entry : PixelBuy.get().getStore().getItems().entrySet()) {
            final StoreItem item = entry.getValue();
            Lang.COMMAND_STORE_ITEMS_INFO.sendTo(sender, itemNum, item.getId(), item.getPrice(), item.isOnline());
            item.getOnBuy().forEach(action -> Lang.COMMAND_STORE_ITEMS_ENUM.sendTo(sender, action.asString()));
            itemNum++;
        }
    }
}
