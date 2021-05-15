package com.minelatino.pixelbuy.util;

import com.minelatino.pixelbuy.PixelBuy;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemBuilder {

    private Material material;
    private short durability = 0;
    private int amount;
    private String name;
    private List<String> lore;
    private int customModelData = 0;
    private Map<Enchantment, Integer> enchantments;

    public ItemBuilder(String material) {
        this(material, 1);
    }

    public ItemBuilder(String material, int amount) {
        this(material, (short) 0, amount);
    }

    public ItemBuilder(String material, short durability, int amount) {
        Material mat = Material.getMaterial(material);
        this.material = (mat != null ? mat : Material.AIR);
        this.durability = durability;
        this.amount = amount;
    }

    public ItemBuilder(Map<String, String> keys) {
        Material mat = Material.getMaterial(keys.get("material"));
        this.material = (mat != null ? mat : Material.AIR);
        amount = PixelUtils.parseInt(keys.getOrDefault("amount", "1"), 1);
        if (keys.containsKey("durability")) {
            durability = (short) PixelUtils.parseInt(keys.get("durability"), 0);
        }
        if (keys.containsKey("name")) {
            name = PixelBuy.LOCALE.color(keys.get("name"));
        }
        if (keys.containsKey("lore")) {
            lore = PixelBuy.LOCALE.color(new ArrayList<>(Arrays.asList(keys.get("lore").split("\\n"))));
        }
        if (keys.containsKey("custommodeldata")) {
            customModelData = PixelUtils.parseInt(keys.get("custommodeldata"), 0);
        }
        if (keys.containsKey("enchantments")) {
            enchantments = new HashMap<>();
            Map<String, String> map = PixelUtils.getKeys(keys.get("enchantments"), ",", "=");
            map.forEach((e, level) -> {
                Enchantment enchant = Enchantment.getByName(e.toUpperCase());
                if (enchant != null) {
                    enchantments.put(enchant, PixelUtils.parseInt(level, 1));
                }
            });
        }
    }

    public ItemBuilder material(String material) {
        Material mat = Material.getMaterial(material);
        this.material = (mat != null ? mat : Material.AIR);
        return this;
    }

    public ItemBuilder durability(short durability) {
        this.durability = durability;
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder customModelData(int customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public ItemBuilder enchantment(String enchant, int level) {
        Enchantment e = Enchantment.getByName(enchant);
        if (e != null) {
            enchantments.put(e, level);
        }
        return this;
    }

    public ItemBuilder enchantments(Map<String, String> enchantments) {
        enchantments.forEach((e, level) -> {
            Enchantment enchant = Enchantment.getByName(e);
            if (enchant != null) {
                this.enchantments.put(enchant, PixelUtils.parseInt(level, 1));
            }
        });
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        item.setDurability(durability);
        ItemMeta meta = item.getItemMeta();
        if (name != null) meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        if (customModelData != 0) {
            try {
                meta.setCustomModelData(customModelData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        item.setItemMeta(meta);
        item.addUnsafeEnchantments(enchantments);
        return item;
    }
}
