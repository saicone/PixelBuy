package com.saicone.pixelbuy.module.action.type;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.module.action.ActionType;
import com.saicone.pixelbuy.util.Utils;
import io.th0rgal.oraxen.api.OraxenItems;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemAction extends ActionType {

    public static final boolean ORAXEN_COMPATIBILITY;
    public static final boolean MMOITEMS_COMPATIBILITY;
    private static final ItemStack DEFAULT_ITEM;

    public static final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    static {
        ORAXEN_COMPATIBILITY = Bukkit.getPluginManager().isPluginEnabled("Oraxen") || Bukkit.getPluginManager().isPluginEnabled("oraxen");
        MMOITEMS_COMPATIBILITY = Bukkit.getPluginManager().isPluginEnabled("MMOItems") || Bukkit.getPluginManager().isPluginEnabled("mmoitems");
        ItemStack item = new ItemStack(Material.PAPER);
        item.addUnsafeEnchantment(Enchantment.LURE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(Utils.color("&e&lInvalid Item"));
        meta.setLore(Utils.color(Arrays.asList(
                "",
                "&7Reason&8: &f{reason}"
        )));
        item.setItemMeta(meta);
        DEFAULT_ITEM = item;
    }

    @Override
    public String getType() {
        return "ITEM";
    }

    @Override
    public boolean isRefundable() {
        return true;
    }

    @Override
    public void executeBuy(String player, Integer orderID) {
        ItemStack it = getItem(Utils.color(getExecutable(player, orderID)));
        Player p = Utils.getPlayer(player);
        if (p != null) p.getInventory().addItem(it);
    }

    @Override
    public void executeRefund(String player, Integer orderID) {
        ItemStack it = getItem(Utils.color(getExecutable(player, orderID)));
        PixelBuy.get().getEventManager().addItem(it, it.getAmount());
    }

    private ItemStack getItem(String content) {
        return cache.computeIfAbsent(content, this::buildItem);
    }

    @SuppressWarnings("unchecked")
    public ItemStack buildItem(String content) {
        Map<String, Object> map = asMap(content);
        String material = (String) mapValue(map, "material", "mat", "id");
        if (material == null) return buildDefaultItem("There's no material");

        int amount = intOrDefault(mapValueOrDefault(map, 1, "amount", "amt"), 1);
        ItemStack item;
        if (material.toLowerCase().startsWith("oraxen:")) {
            String[] part = material.split(":", 2);
            if (part.length < 2) {
                return buildDefaultItem("Unknown ID for Oraxen item");
            }
            String id = part[1];
            if (OraxenItems.exists(id)) {
                item = OraxenItems.getItemById(id).setAmount(amount).build();
            } else {
                return buildDefaultItem("Invalid ID for Oraxen item");
            }
        } else if (material.toLowerCase().startsWith("mmoitems:") && MMOITEMS_COMPATIBILITY) {
            String[] part = material.split(":", 3);
            if (part.length < 2) {
                return buildDefaultItem("Unknown Type for MMOItems item");
            } else if (part.length < 3) {
                return buildDefaultItem("Unknown ID for MMOItems item");
            }
            String type = part[1];
            String id = part[2];
            item = MMOItems.plugin.getItem(type, id);
            if (item == null) {
                return buildDefaultItem("Invalid MMOItems item");
            }
        } else {
            Material mat = Material.getMaterial(material);
            if (mat != null) {
                item = new ItemStack(mat, amount);
            } else {
                return buildDefaultItem("Unknown material");
            }
        }

        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        if (meta == null) {
            return buildDefaultItem("Invalid item meta");
        }
        Object mapValue;
        if ((mapValue = mapValueOrDefault(map, 0, "custommodeldata", "modeldata", "model")) != null) {
            int model = intOrDefault(mapValue, 0);
            if (model > 0 && Utils.verNumber >= 14) {
                meta.setCustomModelData(model);
            }
        }
        if ((mapValue = mapValue(map, "enchantments", "enchants", "ench")) != null) {
            try {
                Map<Enchantment, Integer> enchants = (Map<Enchantment, Integer>) mapValue;
                enchants.forEach((enchant, level) -> meta.addEnchant(enchant, level, true));
            } catch (ClassCastException e) {
                return buildDefaultItem("Cannot set enchantments");
            }
        }
        if ((mapValue = mapValue(map, "description", "lorelines", "lore")) != null) {
            try {
                List<String> lore = (List<String>) mapValue;
                meta.setLore(lore);
            } catch (ClassCastException e) {
                return buildDefaultItem("Cannot set lore");
            }
        }
        if ((mapValue = mapValue(map, "add-description", "add-lorelines", "add-lore")) != null) {
            try {
                List<String> lore = (List<String>) mapValue;
                List<String> metaLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                metaLore.addAll(lore);
                meta.setLore(metaLore);
            } catch (ClassCastException e) {
                return buildDefaultItem("Cannot add lore");
            }
        }
        if ((mapValue = mapValue(map, "displayname", "name")) != null) {
            String name = String.valueOf(mapValue);
            meta.setDisplayName(name);
        }
        if ((mapValue = mapValue(map, "add-displayname", "add-name")) != null) {
            String name = String.valueOf(mapValue);
            meta.setDisplayName((meta.hasDisplayName() ? meta.getDisplayName() : "") + name);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildDefaultItem(String reason) {
        ItemStack item = new ItemStack(DEFAULT_ITEM);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Utils.replace(meta.getLore(), "{reason}", reason));
        item.setItemMeta(meta);
        return item;
    }

    public static Map<String, Object> asMap(String string) {
        Map<String, Object> map = new HashMap<>();
        for (String s : string.replace("<,>", "{pixelbuy:comma}").split(",")) {
            String[] split = s.split(":", 2);
            if (split.length > 1) {
                String key = split[0].trim().toLowerCase();
                if (key.isEmpty()) continue;
                String value = split[1].replace("{pixelbuy:comma}", ",").replace("<|>", "{pixelbuy:separator}");
                if (!value.isEmpty() && value.charAt(0) == ' ') {
                    value = value.substring(1);
                }
                if (key.contains("description") || key.contains("lore")) {
                    List<String> lore = new ArrayList<>();
                    for (String line : value.split("\\|")) {
                        lore.add(line.replace("{pixelbuy:separator}", "|"));
                    }
                    map.put(key, lore);
                } else if (key.contains("enchant")) {
                    Map<Enchantment, Integer> enchants = new HashMap<>();
                    for (String val : value.split("\\|")) {
                        String[] enchant = val.split("=", 2);
                        if (enchant.length > 1) {
                            Enchantment ent = Enchantment.getByName(enchant[0].trim().replace("{pixelbuy:separator}", "|").toUpperCase());
                            if (ent != null) {
                                enchants.put(ent, intOrDefault(enchant[1].trim(), 0));
                            }
                        }
                    }
                    map.put(key, enchants);
                } else if (key.contains("amount") || key.contains("amt")) {
                    map.put(key, intOrDefault(value.trim(), 1));
                } else if (key.contains("model")) {
                    map.put(key, intOrDefault(value.trim(), 0));
                } else {
                    map.put(key, value.replace("{pixelbuy:separator}", "|"));
                }
            }
        }
        return map;
    }

    private static Object mapValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private static Object mapValueOrDefault(Map<String, Object> map, Object def, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return def;
    }

    private static Integer intOrDefault(Object o, int def) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else {
            try {
                return Integer.parseInt(String.valueOf(o));
            } catch (NumberFormatException e) {
                return def;
            }
        }
    }
}
