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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PixelStore {

    private static final List<StoreAction> ACTION_TYPES = Arrays.asList(
            new BroadcastAction(),
            new CommandAction(),
            new ItemAction(),
            new MessageAction()
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
        ItemAction.cache.clear();
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
            items.add(new StoreItem(identifier, store.getString("Items." + identifier + ".price"), store.getBoolean("Items." + identifier + ".online", true), store.getStringList("Items." + identifier + ".execute")));
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

    @Nullable
    public static StoreAction parseAction(@NotNull String input, @NotNull String price) {
        final String type = input.split(":", 2)[0].toUpperCase();
        final StoreAction action = ACTION_TYPES.stream().filter(a -> a.getType().equals(type)).findFirst().orElse(null);
        if (action != null) {
            action.setParts(input.split(":", 2)[1], price);
            return action;
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
