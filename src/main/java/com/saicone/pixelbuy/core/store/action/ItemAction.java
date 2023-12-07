package com.saicone.pixelbuy.core.store.action;

import com.google.gson.Gson;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.module.settings.SettingsItem;
import com.saicone.pixelbuy.util.MStrings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    private final SettingsItem item;

    public ItemAction(@NotNull SettingsItem item) {
        this.item = item;
    }

    @NotNull
    public SettingsItem getItem() {
        return item;
    }

    @Override
    public void run(@NotNull StoreClient client, int amount) {
        if (client.isOnline()) {
            final Player player = client.getPlayer();
            for (int i = 0; i < amount; i++) {
                final int count = i + 1;
                giveItem(player, s -> client.parse(s.replace("{action_count}", String.valueOf(count))));
            }
        }
    }

    private void giveItem(@NotNull Player player, @NotNull Function<String, String> function) {
        ItemStack item;
        try {
            item = getItem().parse(function).build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            item = buildDefaultItem("Caught exception while building");
        }
        if (item.getType() == Material.AIR) {
            item = buildDefaultItem("Item material cannot be AIR");
        }
        final Map<Integer, ItemStack> items = player.getInventory().addItem(item);
        if (!items.isEmpty()) {
            if (Bukkit.isPrimaryThread()) {
                for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                    player.getWorld().dropItem(player.getLocation(), entry.getValue());
                }
            } else {
                Bukkit.getScheduler().runTask(PixelBuy.get(), () -> {
                    for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                        player.getWorld().dropItem(player.getLocation(), entry.getValue());
                    }
                });
            }
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(getItem().asMap());
    }

    @NotNull
    public static ItemStack buildDefaultItem(@NotNull String reason) {
        final ItemStack item = new ItemStack(Material.PAPER);
        item.addUnsafeEnchantment(Enchantment.LURE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(MStrings.color("&c&lInvalid Item"));
        meta.setLore(MStrings.color(List.of("&7Reason&8: &f" + reason)));
        item.setItemMeta(meta);
        return item;
    }
}
