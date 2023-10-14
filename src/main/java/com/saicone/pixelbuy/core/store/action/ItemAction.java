package com.saicone.pixelbuy.core.store.action;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.util.MStrings;
import io.th0rgal.oraxen.api.OraxenItems;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemAction extends StoreAction {

    public static final boolean ORAXEN_COMPATIBILITY;
    public static final boolean MMOITEMS_COMPATIBILITY;
    private static final ItemStack DEFAULT_ITEM;

    public static final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    static {
        ORAXEN_COMPATIBILITY = Bukkit.getPluginManager().isPluginEnabled("Oraxen") || Bukkit.getPluginManager().isPluginEnabled("oraxen");
        MMOITEMS_COMPATIBILITY = Bukkit.getPluginManager().isPluginEnabled("MMOItems") || Bukkit.getPluginManager().isPluginEnabled("mmoitems");
        final ItemStack item = new ItemStack(Material.PAPER);
        item.addUnsafeEnchantment(Enchantment.LURE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(MStrings.color("&e&lInvalid Item"));
        meta.setLore(MStrings.color(Arrays.asList(
                "",
                "&7Reason&8: &f{reason}"
        )));
        item.setItemMeta(meta);
        DEFAULT_ITEM = item;
    }

    @Override
    public @NotNull String getType() {
        return "ITEM";
    }

    @Override
    public boolean isRefundable() {
        return true;
    }

    @Override
    public void executeBuy(@NotNull String player, int orderID) {
        final ItemStack item = getItem(MStrings.color(getExecutable(player, orderID)));
        final Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null) {
            onlinePlayer.getInventory().addItem(item);
        }
    }

    @Override
    public void executeRefund(@NotNull String player, int orderID) {
        final ItemStack item = getItem(MStrings.color(getExecutable(player, orderID)));
        PixelBuy.get().getListener().addItem(item, item.getAmount());
    }

    @NotNull
    private ItemStack getItem(@NotNull String content) {
        return cache.computeIfAbsent(content, this::buildItem);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public ItemStack buildItem(@NotNull String content) {
        final Map<String, Object> map = asMap(content);
        final String material = (String) mapValue(map, "material", "mat", "id");
        if (material == null) {
            return buildDefaultItem("There's no material");
        }

        final int amount = intOrDefault(mapValueOrDefault(map, 1, "amount", "amt"), 1);
        final ItemStack item;
        if (material.toLowerCase().startsWith("oraxen:")) {
            final String[] part = material.split(":", 2);
            if (part.length < 2) {
                return buildDefaultItem("Unknown ID for Oraxen item");
            }
            final String id = part[1];
            if (OraxenItems.exists(id)) {
                item = OraxenItems.getItemById(id).setAmount(amount).build();
            } else {
                return buildDefaultItem("Invalid ID for Oraxen item");
            }
        } else if (material.toLowerCase().startsWith("mmoitems:") && MMOITEMS_COMPATIBILITY) {
            final String[] part = material.split(":", 3);
            if (part.length < 2) {
                return buildDefaultItem("Unknown Type for MMOItems item");
            } else if (part.length < 3) {
                return buildDefaultItem("Unknown ID for MMOItems item");
            }
            final String type = part[1];
            final String id = part[2];
            item = MMOItems.plugin.getItem(type, id);
            if (item == null) {
                return buildDefaultItem("Invalid MMOItems item");
            }
        } else {
            final Material mat = Material.getMaterial(material);
            if (mat != null) {
                item = new ItemStack(mat, amount);
            } else {
                return buildDefaultItem("Unknown material");
            }
        }

        final ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        if (meta == null) {
            return buildDefaultItem("Invalid item meta");
        }

        Object mapValue;
        if ((mapValue = mapValueOrDefault(map, 0, "custommodeldata", "modeldata", "model")) != null) {
            int model = intOrDefault(mapValue, 0);
            if (model > 0) {
                meta.setCustomModelData(model);
            }
        }
        if ((mapValue = mapValue(map, "enchantments", "enchants", "ench")) != null) {
            try {
                final Map<Enchantment, Integer> enchants = (Map<Enchantment, Integer>) mapValue;
                enchants.forEach((enchant, level) -> meta.addEnchant(enchant, level, true));
            } catch (ClassCastException e) {
                return buildDefaultItem("Cannot set enchantments");
            }
        }
        if ((mapValue = mapValue(map, "description", "lorelines", "lore")) != null) {
            try {
                final List<String> lore = (List<String>) mapValue;
                meta.setLore(lore);
            } catch (ClassCastException e) {
                return buildDefaultItem("Cannot set lore");
            }
        }
        if ((mapValue = mapValue(map, "add-description", "add-lorelines", "add-lore")) != null) {
            try {
                final List<String> lore = (List<String>) mapValue;
                final List<String> metaLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                metaLore.addAll(lore);
                meta.setLore(metaLore);
            } catch (ClassCastException e) {
                return buildDefaultItem("Cannot add lore");
            }
        }
        if ((mapValue = mapValue(map, "displayname", "name")) != null) {
            final String name = String.valueOf(mapValue);
            meta.setDisplayName(name);
        }
        if ((mapValue = mapValue(map, "add-displayname", "add-name")) != null) {
            final String name = String.valueOf(mapValue);
            meta.setDisplayName((meta.hasDisplayName() ? meta.getDisplayName() : "") + name);
        }
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    private ItemStack buildDefaultItem(@NotNull String reason) {
        final ItemStack item = new ItemStack(DEFAULT_ITEM);
        final ItemMeta meta = item.getItemMeta();
        final List<String> finalLore = new ArrayList<>();
        for (String s : meta.getLore()) {
            finalLore.add(s.replace("{reason}", reason));
        }
        meta.setLore(finalLore);
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public static Map<String, Object> asMap(@NotNull String string) {
        final Map<String, Object> map = new HashMap<>();
        for (String s : string.replace("<,>", "{pixelbuy:comma}").split(",")) {
            final String[] split = s.split(":", 2);
            if (split.length > 1) {
                final String key = split[0].trim().toLowerCase();
                if (key.isEmpty()) continue;
                String value = split[1].replace("{pixelbuy:comma}", ",").replace("<|>", "{pixelbuy:separator}");
                if (!value.isEmpty() && value.charAt(0) == ' ') {
                    value = value.substring(1);
                }
                if (key.contains("description") || key.contains("lore")) {
                    final List<String> lore = new ArrayList<>();
                    for (String line : value.split("\\|")) {
                        lore.add(line.replace("{pixelbuy:separator}", "|"));
                    }
                    map.put(key, lore);
                } else if (key.contains("enchant")) {
                    final Map<Enchantment, Integer> enchants = new HashMap<>();
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

    @Nullable
    private static Object mapValue(@NotNull Map<String, Object> map, @NotNull String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    @Nullable
    @Contract("_, !null, _ -> !null")
    private static Object mapValueOrDefault(@NotNull Map<String, Object> map, @Nullable Object def, @NotNull String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return def;
    }

    private static int intOrDefault(@NotNull Object object, int def) {
        if (object instanceof Number) {
            return ((Number) object).intValue();
        } else {
            try {
                return Integer.parseInt(String.valueOf(object));
            } catch (NumberFormatException e) {
                return def;
            }
        }
    }
}
