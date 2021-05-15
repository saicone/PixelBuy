package com.minelatino.pixelbuy.api.action.type;

import com.minelatino.pixelbuy.api.action.ActionType;
import com.minelatino.pixelbuy.util.ItemBuilder;
import com.minelatino.pixelbuy.util.PixelUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GiveItemAction extends ActionType {

    public GiveItemAction(String regex) {
        super(regex);
    }

    @Override
    public String getName() {
        return "GIVEITEM";
    }

    @Override
    public boolean executeOnline(Object player, Map<String, String> keys, Map<String, String> content) {
        ItemStack item = new ItemBuilder((content.size() == 1 && content.containsKey("content") ? PixelUtils.getKeys(content.get("content"), ";", ":") : content)).build();
        ((Player) player).getInventory().addItem(item);
        return true;
    }

    @Override
    public boolean executeOffline(Object offlinePlayer, Map<String, String> keys, Map<String, String> content) {
        // TODO: Make a item vault
        return false;
    }
}
