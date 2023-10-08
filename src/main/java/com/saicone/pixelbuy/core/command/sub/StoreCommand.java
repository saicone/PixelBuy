package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.core.command.SubCommand;
import com.saicone.pixelbuy.api.object.StoreItem;
import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class StoreCommand extends SubCommand {

    private final PixelBuy pl = PixelBuy.get();

    @Override
    public Pattern getAliases() {
        return Pattern.compile("(web)?store");
    }

    @Override
    public String getPermission() {
        return pl.getFiles().getConfig().getString("Perms.Store", "pixelbuy.store");
    }

    @Override
    public void execute(CommandSender sender, String cmd, String[] args) {
        if (args.length == 1) {
            Lang.COMMAND_STORE_HELP.sendTo(sender, cmd);
            return;
        }
        int itemNum = 1;
        for (StoreItem item : pl.getStore().getItems()) {
            Lang.COMMAND_STORE_ITEMS_INFO.sendTo(sender, itemNum, item.getIdentifier(), item.getPrice(), item.isOnline());
            item.getActions().forEach(action -> Lang.COMMAND_STORE_ITEMS_ENUM.sendTo(sender, action));
            itemNum++;
        }
    }
}
