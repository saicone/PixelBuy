package com.saicone.pixelbuy.module.settings;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.module.hook.CustomItems;
import com.saicone.pixelbuy.util.ConfigTag;
import com.saicone.pixelbuy.util.MStrings;
import com.saicone.pixelbuy.util.Strings;
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
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SettingsItem extends BukkitSettings {

    @NotNull
    public static SettingsItem of(@Nullable Object object) {
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

    @NotNull
    public String getProvider() {
        final Object object = get(settings -> settings.getIgnoreCase("material"));
        if (object instanceof ConfigurationSection) {
            final BukkitSettings material = BukkitSettings.of(object);
            return material.getRegex("(?i)provider|plugin|custom|from").asString("minecraft");
        } else if (object instanceof String) {
            final int index = ((String) object).indexOf(':');
            if (index > 0) {
                return ((String) object).substring(0, index);
            }
        }
        return "minecraft";
    }

    @Nullable
    public ItemStack getProvidedItem() throws IllegalArgumentException {
        final Object object = get(settings -> settings.getIgnoreCase("material"));
        ItemStack provided = null;
        if (object instanceof ConfigurationSection) {
            final BukkitSettings material = BukkitSettings.of(object);
            final String id = material.getIgnoreCase("id").asString();
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Cannot create ItemStack with empty ID");
            }
            final String provider = material.getRegex("(?i)provider|plugin|custom|from").asString();
            if (provider != null && !provider.isBlank()) {
                switch (provider.trim().toLowerCase()) {
                    case "oraxen":
                        provided = CustomItems.fromOraxen(id);
                        break;
                    case "mmoitems":
                        final String type = material.getRegex("(?i)type|category|group").asString();
                        if (type == null) {
                            throw new IllegalArgumentException("The MMOItem provider require 'type' configuration");
                        }
                        provided = CustomItems.fromMMOItems(type, id);
                        break;
                    case "itemsadder":
                        provided = CustomItems.fromItemsAdder(id);
                        break;
                    default:
                        throw new IllegalArgumentException("The item provider '" + provider + "' doesn't exist");
                }
            } else {
                set(material.getName(), id);
            }
        } else if (object != null) {
            provided = CustomItems.from(String.valueOf(object));
        } else {
            throw new IllegalArgumentException("Cannot create ItemStack with empty material");
        }
        if (provided != null) {
            set("material", null);
        }
        return provided;
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
        if (PixelBuy.get().getLang().getLogLevel() >= 4) {
            PixelBuy.log(4, "Building item: " + asMap());
        }
        ItemStack item = getProvidedItem();
        if (item == null) {
            item = new ItemStack(XMaterial.STONE.parseMaterial());
        }

        final String matStr = this.getString("material");
        if (matStr != null && !matStr.isBlank()) {
            final Optional<XMaterial> materialOpt = XMaterial.matchXMaterial(matStr);
            if (materialOpt.isEmpty()) {
                PixelBuy.log(2, "Cannot find the material: " + matStr);
                set("material", null);
                item = new ItemStack(XMaterial.STONE.parseMaterial());
            }
        }

        if (XMaterial.supports(11) && item.hasItemMeta() && item.getItemMeta().isUnbreakable()) {
            set("unbreakable", true);
        }
        XItemStack.edit(item, this, MStrings::color, null);

        final BukkitSettings append = getConfigurationSection(settings -> settings.getIgnoreCase("append"));
        if (item.hasItemMeta() && append != null) {
            final ItemMeta meta = item.getItemMeta();

            final BukkitSettings appendName = append.getConfigurationSection(settings -> settings.getIgnoreCase("name"));
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

            final BukkitSettings appendLore = append.getConfigurationSection(settings -> settings.getIgnoreCase("lore"));
            if (appendLore != null) {
                final List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

                for (String key : appendLore.getKeys(false)) {
                    if (Strings.isNumber(key)) {
                        try {
                            final int index = Integer.parseInt(key) - 1;
                            if (index >= 0 && index < lore.size()) {
                                final List<String> lines = appendLore.getAny(key).asList(type -> {
                                    final String s = type.asString();
                                    return s == null ? null : MStrings.color(s);
                                });
                                lore.addAll(index, lines);
                            } else {
                                throw new IllegalArgumentException("Trying to apply lore at index " + index + " while lore size is " + lore.size());
                            }
                        } catch (NumberFormatException e) {
                            PixelBuy.logException(2, e);
                        }
                    }
                }

                final List<String> loreBefore = appendLore.getIgnoreCase("before").asList(type -> {
                    final String s = type.asString();
                    return s == null ? null : MStrings.color(s);
                });
                if (!loreBefore.isEmpty()) {
                    lore.addAll(0, loreBefore);
                }

                final List<String> loreAfter = appendLore.getIgnoreCase("after").asList(type -> {
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

            item.setItemMeta(meta);
        }

        final BukkitSettings nbt = getConfigurationSection(settings -> settings.getIgnoreCase("nbt"));
        if (nbt != null) {
            return RtagItem.edit(item, tag -> {
                tag.deepMerge(ConfigTag.fromConfigValue(nbt.asMap()), true);
            });
        }

        return item;
    }
}
