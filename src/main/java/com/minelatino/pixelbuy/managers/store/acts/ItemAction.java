package com.minelatino.pixelbuy.managers.store.acts;

import com.minelatino.pixelbuy.managers.store.ActionType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemAction extends ActionType {

    @Override
    public String getType() {
        return "ITEM";
    }

    @Override
    public boolean isRefundable() {
        return true;
    }

    @Override
    public void executeBuy(Player player, Integer orderID) {
        ItemStack it = getItem(getExecutable(player.getName(), orderID));
        player.getInventory().addItem(it);
    }

    @Override
    public void executeRefund(Player player, Integer orderID) {
        ItemStack it = getItem(getExecutable(player.getName(), orderID));

    }

    private ItemStack getItem(String content) {
        String[] parts = content.split(",");
        Material mat = Material.STONE;
        int amount = 1;
        String name = null;
        List<String> lore = new ArrayList<>();
        Map<Enchantment, Integer> enchants = new HashMap<>();
        for (String part : parts) {
            String[] s = part.split(":", 2);
            switch (s[0].toLowerCase()) {
                case "mat":
                case "material":
                    mat = Material.matchMaterial(s[1]);
                    break;
                case "amount":
                case "amt:":
                    amount = Integer.parseInt(s[1]);
                    break;
                case "name":
                    name = s[1].replace('&', '§');
                    break;
                case "lore":
                    lore = Arrays.asList(s[1].replace('&', '§').split("\\|"));
                    break;
                case "enchants":
                case "enchantments":
                    for (String enchant : s[1].split("\\|")) {
                        String[] e = enchant.split("=", 2);
                        Integer lvl = (e.length == 1 ? 1 : Integer.parseInt(e[1]));
                        enchants.put(Enchantment.getByName(e[0].toUpperCase()), lvl);
                    }
                    break;
            }
        }
        if (mat == null) mat = Material.STONE;
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        enchants.forEach((enchant, lvl) -> meta.addEnchant(enchant, lvl, true));
        item.setItemMeta(meta);
        return item;
    }
}
