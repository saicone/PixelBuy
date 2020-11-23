package com.minelatino.pixelbuy.command.sub;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.store.StoreItem;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.command.CommandSender;

public class StoreCommand {

    private final PixelBuy pl;

    public StoreCommand(PixelBuy pl) {
        this.pl = pl;
    }

    public boolean execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            pl.langStringList("Command.Store.Help").forEach(string -> s.sendMessage(Utils.color(string)));
            return true;
        }
        int itemNum = 1;
        for (StoreItem item : pl.getStore().getItems()) {
            for (String string : pl.langStringList("Command.Store.Items.Info")) {
                s.sendMessage(Utils.color(string.replace("%num%", String.valueOf(itemNum))
                                .replace("%ID%", item.getIdentifier())
                                .replace("%price%", item.getPrice())
                                .replace("%online%", String.valueOf(item.isOnline()))));
            }
            item.getActions().forEach(action -> s.sendMessage(Utils.color(pl.langString("Command.Store.Items.Enum").replace("%action%", action.getExecutable()))));
            itemNum++;
        }
        return true;
    }
}
