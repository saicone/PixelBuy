package com.saicone.pixelbuy.module.hook;

import com.bgsoftware.wildtools.api.WildToolsAPI;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.google.common.base.Suppliers;
import fakeapi.FakeApi1;
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

    private static final Supplier<Boolean> NEXO = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("Nexo"));
    private static final Supplier<Boolean> ORAXEN = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("Oraxen"));
    private static final Supplier<Boolean> MMOITEMS = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("MMOItems"));
    private static final Supplier<Boolean> ITEMSADDER = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("ItemsAdder"));
    private static final Supplier<Boolean> WILDTOOLS = Suppliers.memoize(() -> Bukkit.getPluginManager().isPluginEnabled("WildTools"));

    public static boolean isNexoCompatible() {
        return NEXO.get();
    }

    public static boolean isOraxenCompatible() {
        return ORAXEN.get();
    }

    public static boolean isMMOItemsCompatible() {
        return MMOITEMS.get();
    }

    public static boolean isItemsAdderCompatible() {
        return ITEMSADDER.get();
    }

    public static boolean isWildToolsCompatible() {
        return WILDTOOLS.get();
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
            case "nexo":
                return fromNexo(material);
            case "oraxen":
                return fromOraxen(material);
            case "mmoitem":
            case "mmoitems":
                final String[] split = material.split(":");
                return fromMMOItems(split[0], split[1]);
            case "itemsadder":
                return fromItemsAdder(material);
            case "wildtools":
                return fromWildTools(material);
            default:
                return null;
        }
    }

    @Nullable
    public static ItemStack fromNexo(@NotNull String id) {
        if (NEXO.get()) {
            final var builder = FakeApi1.itemFromId(id);
            if (builder != null) {
                return builder.build();
            }
        }
        return null;
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

    @Nullable
    public static ItemStack fromWildTools(@NotNull String name) {
        if (WILDTOOLS.get()) {
            final Tool tool = WildToolsAPI.getTool(name);
            return tool.getItemStack();
        }
        return null;
    }
}
