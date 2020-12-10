package com.minelatino.pixelbuy.command.sub;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.command.SubCommand;
import com.minelatino.pixelbuy.managers.store.StoreItem;
import com.minelatino.pixelbuy.util.Utils;
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
            pl.langStringList("Command.Store.Help").forEach(string -> sender.sendMessage(Utils.color(string.replace("%cmd%", cmd))));
            return;
        }
        int itemNum = 1;
        for (StoreItem item : pl.getStore().getItems()) {
            for (String string : pl.langStringList("Command.Store.Items.Info")) {
                sender.sendMessage(Utils.color(string.replace("%num%", String.valueOf(itemNum))
                                .replace("%ID%", item.getIdentifier())
                                .replace("%price%", item.getPrice())
                                .replace("%online%", String.valueOf(item.isOnline()))));
            }
            item.getActions().forEach(action -> sender.sendMessage(Utils.color(pl.langString("Command.Store.Items.Enum").replace("%action%", action))));
            itemNum++;
        }
    }
}
