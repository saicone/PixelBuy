package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.PixelBuyAPI;
import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.module.settings.SettingsItem;
import com.saicone.pixelbuy.util.OptionalType;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StoreItem {

    // Information
    private final String id;
    private Map<String, Object> product = Map.of();
    private Set<String> categories = Set.of();
    private Float price = 0.0f;

    // Gui
    private ItemStack display;

    // Options
    private boolean online;
    private boolean alwaysRun;
    private boolean alwaysSave;
    private Set<String> append = Set.of();

    // Executions
    private List<StoreAction> onBuy;
    private List<StoreAction> onRecover;
    private List<StoreAction> onRefund;

    public StoreItem(@NotNull String id) {
        this.id = id;
    }

    public void onReload(@NotNull BukkitSettings config) {
        this.product = new HashMap<>();
        final ConfigurationSection product = config.getConfigurationSection(settings -> settings.getIgnoreCase("product"));
        if (product != null) {
            for (String key : product.getKeys(false)) {
                this.product.put(key, product.get(key));
            }
        }
        this.categories = config.getRegex("(?i)categor(y|ies)").asCollection(new HashSet<>(), OptionalType::asString);
        this.price = config.getIgnoreCase("price").asFloat(0.0f);
        final SettingsItem displayItem = config.getItem(settings -> settings.getRegex("(?i)display(-?item)?"));
        if (displayItem == null) {
            this.display = null;
        } else {
            final SettingsItem item = displayItem.parse(s -> Strings.replaceBracketPlaceholder(s, s1 -> s1.equals("store"), (id, arg) -> {
                final String field = arg.toLowerCase();
                if (field.startsWith("item_")) {
                    return get(field.substring(5));
                } else {
                    return PixelBuy.get().getStore().get(field);
                }
            }));
            if (item.getProvider().equalsIgnoreCase("mmoitems") && !Bukkit.isPrimaryThread()) {
                // MMOItems only can be handled synchronously
                Bukkit.getScheduler().runTask(PixelBuy.get(), () -> this.display = item.build());
            } else {
                this.display = item.build();
            }
        }
        this.online = config.getIgnoreCase("options", "online").asBoolean(false);
        this.alwaysRun = config.getIgnoreCase("options", "alwaysrun").asBoolean(false);
        this.alwaysSave = config.getIgnoreCase("options", "alwayssave").asBoolean(true);
        this.append = config.getIgnoreCase("options", "append").asCollection(new HashSet<>(), OptionalType::asString);
        this.onBuy = PixelBuyAPI.buildActions(config.getIgnoreCase("onBuy").getValue());
        this.onRecover = PixelBuyAPI.buildActions(config.getIgnoreCase("onRecover").getValue());
        if (this.onRecover.isEmpty()) {
            this.onRecover = null;
        }
        this.onRefund = PixelBuyAPI.buildActions(config.getIgnoreCase("onRefund").getValue());
        if (this.onRefund.isEmpty()) {
            this.onRefund = null;
        }
    }

    @Nullable
    public Object get(@NotNull String field) {
        switch (field) {
            case "id":
                return id;
            case "category":
                return categories.size() > 0 ? categories.iterator().next() : null;
            case "categories":
                return String.join("\n", categories);
            case "categories_size":
                return categories.size();
            case "price":
                return price;
            default:
                return null;
        }
    }

    @NotNull
    public String getId() {
        return id;
    }

    @Nullable
    public Object getProduct(@NotNull String provider) {
        return product.get(provider);
    }

    @NotNull
    public Set<String> getCategories() {
        return categories;
    }

    public float getPrice() {
        return price;
    }

    @Nullable
    public ItemStack getDisplay() {
        return display;
    }

    public Set<String> getAppend() {
        return append;
    }

    @NotNull
    public List<StoreAction> getOnBuy() {
        return onBuy;
    }

    @Nullable
    public List<StoreAction> getOnRecover() {
        return onRecover;
    }

    @Nullable
    public List<StoreAction> getOnRefund() {
        return onRefund;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isAlwaysRun() {
        return alwaysRun;
    }

    public boolean isAlwaysSave() {
        return alwaysSave;
    }

    public void onBuy(@NotNull StoreClient client, int amount) {
        for (StoreAction action : onBuy) {
            action.run(client, amount);
        }
    }

    public void onRecover(@NotNull StoreClient client, int amount) {
        if (onRecover == null) {
            onBuy(client, amount);
            return;
        }
        for (StoreAction action : onRecover) {
            action.run(client, amount);
        }
    }

    public void onRefund(@NotNull StoreClient client, int amount) {
        if (onRefund == null) {
            return;
        }
        for (StoreAction action : onRefund) {
            action.run(client, amount);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof StoreOrder.Item) return id.equals(((StoreOrder.Item) o).getId());
        if (o == null || getClass() != o.getClass()) return false;

        StoreItem storeItem = (StoreItem) o;

        return id.equals(storeItem.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
