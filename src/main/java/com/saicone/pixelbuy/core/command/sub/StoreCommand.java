package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.SubCommand;
import com.saicone.pixelbuy.core.store.StoreItem;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class StoreCommand extends SubCommand {

    private final PixelBuy plugin = PixelBuy.get();

    @Override
    public @NotNull Pattern getAliases() {
        return Pattern.compile("(web)?store");
    }

    @Override
    public @NotNull String getPermission() {
        return PixelBuy.settings().getString("Perms.Store", "pixelbuy.store");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String cmd, @NotNull String[] args) {
        if (args.length == 1) {
            Lang.COMMAND_STORE_HELP.sendTo(sender, cmd);
            return;
        }
        int itemNum = 1;
        for (var entry : plugin.getStore().getItems().entrySet()) {
            final StoreItem item = entry.getValue();
            Lang.COMMAND_STORE_ITEMS_INFO.sendTo(sender, itemNum, item.getId(), item.getPrice(), item.isOnline());
            item.getOnBuy().forEach(action -> Lang.COMMAND_STORE_ITEMS_ENUM.sendTo(sender, action.asString()));
            itemNum++;
        }
    }
}
