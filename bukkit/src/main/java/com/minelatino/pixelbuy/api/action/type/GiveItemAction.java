package com.minelatino.pixelbuy.api.action.type;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.api.action.ActionType;
import com.minelatino.pixelbuy.util.PixelUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
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
    public boolean executeOnline(Object player, Map<String, String> keys, String content) {
        Map<String, String> itemKeys = PixelUtils.getKeys(content, ";", ":");
        Material mat = Material.getMaterial(itemKeys.get("material"));
        if (mat == null) return false;
        ItemStack item = new ItemStack(mat, PixelUtils.parseInt(itemKeys.getOrDefault("amount", "1"), 1));
        ItemMeta meta = item.getItemMeta();
        if (itemKeys.containsKey("name")) {
            meta.setDisplayName(PixelBuy.LOCALE.color(itemKeys.get("name")));
        }
        if (itemKeys.containsKey("lore")) {
            meta.setLore(PixelBuy.LOCALE.color(new ArrayList<>(Arrays.asList(itemKeys.get("lore").split("\\n")))));
        }
        if (itemKeys.containsKey("custommodeldata")) {
            try {
                meta.setCustomModelData(PixelUtils.parseInt(itemKeys.get("custommodeldata"), 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (itemKeys.containsKey("enchantments")) {

        }
        if (itemKeys.containsKey("flags")) {

        }
        if (itemKeys.containsKey("nbt")) {

        }
        ((Player) player).getInventory().addItem(item);
        return true;
    }

    @Override
    public boolean executeOffline(Object offlinePlayer, Map<String, String> keys, String content) {
        // TODO: Make a item vault
        return false;
    }
}
