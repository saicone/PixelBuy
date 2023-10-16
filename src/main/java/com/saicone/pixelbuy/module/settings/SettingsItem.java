package com.saicone.pixelbuy.module.settings;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.saicone.pixelbuy.module.hook.CustomItems;
import com.saicone.pixelbuy.util.ConfigTag;
import com.saicone.pixelbuy.util.MStrings;
import com.saicone.rtag.RtagItem;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SettingsItem extends BukkitSettings {

    @NotNull
    public static SettingsItem of(@NotNull Object object) {
        if (object instanceof ConfigurationSection) {
            return new SettingsItem((ConfigurationSection) object);
        } else if (object instanceof Map) {
            final SettingsItem settings = new SettingsItem();
            settings.set((Map<?, ?>) object);
            return settings;
        }
        return new SettingsItem();
    }

    public SettingsItem() {
        super();
    }

    public SettingsItem(@Nullable ConfigurationSection delegate) {
        super(delegate);
    }

    @Nullable
    public ItemStack getProvidedItem() throws IllegalArgumentException {
        final BukkitSettings material = getConfigurationSection("material");
        if (material != null) {
            final String id = material.getRegex("(?i)id|type").asString();
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Cannot create ItemStack with empty ID");
            }
            final String provider = material.getRegex("(?i)provider|plugin|custom|from").asString();
            if (provider != null && !provider.isBlank()) {
                switch (provider.trim().toLowerCase()) {
                    case "oraxen":
                        return CustomItems.fromOraxen(id);
                    case "mmoitems":
                        final String type = material.getRegex("(?i)type|category|group").asString();
                        if (type == null) {
                            throw new IllegalArgumentException("The MMOItem provider require 'type' configuration");
                        }
                        return CustomItems.fromMMOItems(type, id);
                    default:
                        throw new IllegalArgumentException("The item provider '" + provider + "' doesn't exist");
                }
            } else {
                set("material", id);
            }
        }
        return null;
    }

    public void set(@NotNull ItemStack item) {
        // Set vanilla tags
        XItemStack.serialize(item, this);
        // Subtract custom tags
        final Map<String, Object> map = subtractCustom(TagCompound.getValue(new RtagItem(item).getTag()),
                TagCompound.getValue(new RtagItem(XItemStack.deserialize(this)).getTag()));
        setNbt(map);
    }

    private static Map<String, Object> subtractCustom(@NotNull Map<String, Object> base, Map<String, Object> temp) {
        final Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : base.entrySet()) {
            if (temp.containsKey(entry.getKey())) {
                final Object value = temp.get(entry.getKey());
                if (TagCompound.isTagCompound(value) && TagCompound.isTagCompound(entry.getValue())) {
                    final Map<String, Object> tempMap = subtractCustom(TagCompound.getValue(entry.getValue()), TagCompound.getValue(value));
                    if (!tempMap.isEmpty()) {
                        map.put(entry.getKey(), tempMap);
                    }
                }
            } else {
                if (TagCompound.isTagCompound(entry.getValue())) {
                    map.put(entry.getKey(), TagCompound.clone(entry.getValue()));
                } else if (TagList.isTagList(entry.getValue())) {
                    map.put(entry.getKey(), TagList.clone(entry.getValue()));
                } else {
                    map.put(entry.getKey(), TagBase.clone(entry.getValue()));
                }
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public void setNbt(@NotNull Map<String, Object> map) {
        createSection("nbt", (Map<String, Object>) ConfigTag.toConfigValue(map));
    }

    @Override
    @Contract("_ -> new")
    public @NotNull SettingsItem parse(@NotNull Function<String, String> function) {
        return parse(this, new SettingsItem(), function);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull SettingsItem parse(@NotNull BiFunction<String, String, String> function) {
        return parse(this, new SettingsItem(), function);
    }

    @NotNull
    public ItemStack build() throws IllegalArgumentException {
        ItemStack item = getProvidedItem();
        if (item == null) {
            item = new ItemStack(XMaterial.NETHER_PORTAL.parseMaterial());
        }

        XItemStack.edit(item, this, MStrings::color, null);

        final BukkitSettings append = getConfigurationSection("append");
        if (item.hasItemMeta() && append != null) {
            final ItemMeta meta = item.getItemMeta();

            final BukkitSettings appendName = append.getConfigurationSection("name");
            if (appendName != null) {
                String name = meta.hasDisplayName() ? meta.getDisplayName() : "";

                final String nameBefore = appendName.getIgnoreCase("before").asString();
                if (nameBefore != null) {
                    name = MStrings.color(nameBefore) + name;
                }

                final String nameAfter = appendName.getIgnoreCase("after").asString();
                if (nameAfter != null) {
                    name = name + MStrings.color(nameAfter);
                }

                if (!name.isEmpty()) {
                    meta.setDisplayName(name);
                }
            }

            final BukkitSettings appendLore = append.getConfigurationSection("lore");
            if (appendLore != null) {
                final List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

                final List<String> loreBefore = append.getIgnoreCase("before").asList(type -> {
                    final String s = type.asString();
                    return s == null ? null : MStrings.color(s);
                });
                if (!loreBefore.isEmpty()) {
                    for (int i = loreBefore.size() - 1; i >= 0; i--) {
                        lore.add(0, loreBefore.get(i));
                    }
                }

                final List<String> loreAfter = append.getIgnoreCase("after").asList(type -> {
                    final String s = type.asString();
                    return s == null ? null : MStrings.color(s);
                });
                if (!loreAfter.isEmpty()) {
                    lore.addAll(loreAfter);
                }

                if (!lore.isEmpty()) {
                    meta.setLore(lore);
                }
            }
        }

        final BukkitSettings nbt = getConfigurationSection("nbt");
        if (nbt != null) {
            return RtagItem.edit(item, tag -> {
                tag.deepMerge(ConfigTag.fromConfigValue(nbt.asMap()), true);
            });
        }

        return item;
    }
}
