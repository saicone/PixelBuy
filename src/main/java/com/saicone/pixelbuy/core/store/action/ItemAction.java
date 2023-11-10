package com.saicone.pixelbuy.core.store.action;

import com.google.gson.Gson;
import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.module.settings.SettingsItem;
import com.saicone.pixelbuy.util.MStrings;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ItemAction extends StoreAction {

    public static final Builder<ItemAction> BUILDER = new Builder<ItemAction>("(?i)(give-?)?items?") {
        @Override
        protected @NotNull BukkitSettings parseSettings(@NotNull Object object) {
            return SettingsItem.of(object);
        }
    }.accept(config -> {
        if (config instanceof SettingsItem) {
            return new ItemAction((SettingsItem) config);
        }
        final SettingsItem item = new SettingsItem();
        item.set(config);
        return new ItemAction(item);
    });

    private static final ItemStack DEFAULT_ITEM;

    static {
        final ItemStack item = new ItemStack(Material.PAPER);
        item.addUnsafeEnchantment(Enchantment.LURE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(MStrings.color("&e&lInvalid Item"));
        item.setItemMeta(meta);
        DEFAULT_ITEM = item;
    }

    private final SettingsItem item;

    public ItemAction(@NotNull SettingsItem item) {
        this.item = item;
    }

    @NotNull
    public SettingsItem getItem() {
        return item;
    }

    @Override
    public void run(@NotNull StoreClient client) {
        if (client.isOnline()) {
            ItemStack item;
            try {
                item = getItem().parse(client::parse).build();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                item = DEFAULT_ITEM;
            }
            if (item.getType() == Material.AIR) {
                item = DEFAULT_ITEM;
            }
            final Player player = client.getPlayer();
            final Map<Integer, ItemStack> items = player.getInventory().addItem(item);
            if (!items.isEmpty()) {
                for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                    player.getWorld().dropItem(player.getLocation(), entry.getValue());
                }
            }
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(getItem().asMap());
    }
}
