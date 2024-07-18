package com.saicone.pixelbuy.module.hook;

import com.google.common.base.Suppliers;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CustomItems {

    private static final Supplier<Boolean> ORAXEN = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("Oraxen"));
    private static final Supplier<Boolean> MMOITEMS = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("MMOItems"));
    private static final Supplier<Boolean> ITEMSADDER = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("ItemsAdder"));

    public static boolean isOraxenCompatible() {
        return ORAXEN.get();
    }

    public static boolean isMMOItemsCompatible() {
        return MMOITEMS.get();
    }

    public static boolean isItemsAdderCompatible() {
        return ITEMSADDER.get();
    }

    @Nullable
    public static ItemStack from(@NotNull String material) {
        final int index = material.indexOf(':');
        if (index > 0) {
            return from(material.substring(0, index), material.substring(index + 1));
        }
        return null;
    }

    @Nullable
    public static ItemStack from(@NotNull String provider, @NotNull String material) {
        switch (provider.toLowerCase()) {
            case "oraxen":
                return fromOraxen(material);
            case "mmoitem":
            case "mmoitems":
                final String[] split = material.split(":");
                return fromMMOItems(split[0], split[1]);
            case "itemsadder":
                return fromItemsAdder(material);
            default:
                return null;
        }
    }

    @Nullable
    public static ItemStack fromOraxen(@NotNull String id) {
        if (ORAXEN.get()) {
            final ItemBuilder builder = OraxenItems.getItemById(id);
            if (builder != null) {
                return builder.build();
            }
        }
        return null;
    }

    @Nullable
    public static ItemStack fromMMOItems(@NotNull String type, @NotNull String id) {
        return MMOITEMS.get() ? MMOItems.plugin.getItem(type, id) : null;
    }

    @Nullable
    public static ItemStack fromItemsAdder(@NotNull String id) {
        if (ITEMSADDER.get()) {
            final CustomStack stack = CustomStack.getInstance(id);
            if (stack != null) {
                return stack.getItemStack();
            }
        }
        return null;
    }
}
