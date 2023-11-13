package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.PixelBuyAPI;
import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.module.settings.SettingsItem;
import com.saicone.pixelbuy.util.OptionalType;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoreItem {

    // Information
    private final String id;
    private Set<String> categories = Set.of();
    private WebValue<Float, Integer> price = WebValue.of(0.0f);

    // Gui
    private ItemStack display;

    // Options
    private boolean online;
    private boolean alwaysRun;
    private Set<String> append = Set.of();

    // Executions
    private List<StoreAction> onBuy;
    private List<StoreAction> onRecover;
    private List<StoreAction> onRefund;

    public StoreItem(@NotNull String id) {
        this.id = id;
    }

    public void onReload(@NotNull BukkitSettings config) {
        this.categories = config.getRegex("(?i)categor(y|ies)").asCollection(new HashSet<>(), OptionalType::asString);
        this.price = WebValue.of(config.getIgnoreCase("price").getValue(), type -> type.asFloat(0.0f), OptionalType::asInt, WebSupervisor::getPrice);
        final SettingsItem item = config.getItem(settings -> settings.getRegex("(?i)display(-?item)?"));
        if (item == null) {
            this.display = null;
        } else {
            this.display = item.parse(s -> Strings.replaceBracketPlaceholder(s, s1 -> s1.equals("store"), (id, arg) -> {
                final String field = arg.toLowerCase();
                if (field.startsWith("item_")) {
                    return get(field.substring(5));
                } else {
                    return PixelBuy.get().getStore().get(field);
                }
            })).build();
        }
        this.online = config.getIgnoreCase("options", "online").asBoolean(false);
        this.alwaysRun = config.getIgnoreCase("options", "alwaysrun").asBoolean(false);
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

    @NotNull
    public Set<String> getCategories() {
        return categories;
    }

    public float getPrice() {
        return price.get(null);
    }

    public float getPrice(@NotNull String provider) {
        return price.get(provider);
    }

    @Nullable
    public Integer getPriceElement(@NotNull String provider) {
        return price.getElement(provider);
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
}
