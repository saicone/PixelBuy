package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.store.action.BroadcastAction;
import com.saicone.pixelbuy.core.store.action.CommandAction;
import com.saicone.pixelbuy.core.store.action.ItemAction;
import com.saicone.pixelbuy.core.store.action.MessageAction;
import com.saicone.pixelbuy.api.store.StoreAction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class PixelStore {

    private static final List<StoreAction.Builder<?>> ACTION_TYPES = Arrays.asList(
            BroadcastAction.BUILDER,
            CommandAction.BUILDER,
            ItemAction.BUILDER,
            MessageAction.BUILDER
    );

    private final PixelBuy plugin = PixelBuy.get();
    private final List<StoreItem> items = new ArrayList<>();

    private FileConfiguration store;
    private String storeName = "";
    private Double discount = 1D;

    public PixelStore() {
        reload(Bukkit.getConsoleSender(), true);
    }

    public void shut() {
        items.clear();
    }

    public void reload(@NotNull CommandSender sender, boolean init) {
        if (!init) {
            items.clear();
        }
        final File file = new File(plugin.getDataFolder(), "store.yml");
        if (!file.exists()) {
            plugin.saveResource("store.yml", false);
        }

        store = YamlConfiguration.loadConfiguration(file);
        storeName = store.getString("Name", "");
        final String discount = store.getString("Global-Discount", "1");
        this.discount = Double.parseDouble("0." + (discount.contains(".") ? discount.split("\\.", 2)[1] : discount.replace("%", "")));
        int count = 0;
        for (String identifier : Objects.requireNonNull(store.getConfigurationSection("Items")).getKeys(false)) {
            items.add(new StoreItem(
                    identifier,
                    store.getString("Items." + identifier + ".price"),
                    store.getBoolean("Items." + identifier + ".online", true),
                    parseAction(store.getList("Items." + identifier + ".onBuy")),
                    parseAction(store.getList("Items." + identifier + ".onRefund"))));
            count++;
        }
        PixelBuy.log(3, count + " store items has been loaded");
    }

    @NotNull
    public List<StoreItem> getItems() {
        return items;
    }

    @NotNull
    public String getStoreName() {
        return storeName;
    }

    @NotNull
    public static List<StoreAction> parseAction(@Nullable Object object) {
        final List<StoreAction> actions = new ArrayList<>();
        if (object == null) {
            return actions;
        }

        if (object instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                final StoreAction action = parseAction(String.valueOf(entry.getKey()), entry.getValue());
                if (action != null) {
                    actions.add(action);
                }
            }
        } else if (object instanceof List) {
            for (Object o : (List<?>) object) {
                actions.addAll(parseAction(o));
            }
        } else {
            final String[] split = String.valueOf(object).split(":", 2);
            final StoreAction action = parseAction(split[0].trim(), split.length > 1 ? split[1].trim() : null);
            if (action != null) {
                actions.add(action);
            }
        }

        return actions;
    }

    @Nullable
    public static StoreAction parseAction(@NotNull String id, @Nullable Object object) {
        for (StoreAction.Builder<?> builder : ACTION_TYPES) {
            if (builder.getPattern().matcher(id).matches()) {
                return builder.build(object);
            }
        }
        return null;
    }

    @Nullable
    public StoreItem getItem(@NotNull String identifier) {
        for (StoreItem item : items) {
            if (item.getIdentifier().equals(identifier)) {
                return item;
            }
        }
        return null;
    }

    public boolean isItem(@NotNull String id) {
        return getItem(id) != null;
    }
}
