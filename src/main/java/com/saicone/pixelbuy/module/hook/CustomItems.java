package com.saicone.pixelbuy.module.hook;

import com.google.common.base.Suppliers;
import io.th0rgal.oraxen.api.OraxenItems;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CustomItems {

    private static final Supplier<Boolean> ORAXEN = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("Oraxen"));
    private static final Supplier<Boolean> MMOITEMS = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("MMOItems"));

    public static boolean isOraxenCompatible() {
        return ORAXEN.get();
    }

    public static boolean isMMOItemsCompatible() {
        return MMOITEMS.get();
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
        final String[] split = material.split(":");
        switch (provider.toLowerCase()) {
            case "oraxen":
                return fromOraxen(split[0]);
            case "mmoitem":
            case "mmoitems":
                return fromMMOItems(split[0], split[1]);
            default:
                return null;
        }
    }

    @Nullable
    public static ItemStack fromOraxen(@NotNull String id) {
        return ORAXEN.get() ? OraxenItems.getItemById(id).build() : null;
    }

    @Nullable
    public static ItemStack fromMMOItems(@NotNull String type, @NotNull String id) {
        return MMOITEMS.get() ? MMOItems.plugin.getItem(type, id) : null;
    }
}
