package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.api.PixelBuyAPI;
import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.module.settings.SettingsItem;
import com.saicone.pixelbuy.util.OptionalType;
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
    private float price = 0.0f;

    // Gui
    private ItemStack display;

    // Options
    private boolean online;

    // Executions
    private List<StoreAction> onBuy;
    private List<StoreAction> onRecover;
    private List<StoreAction> onRefund;

    public StoreItem(@NotNull String id) {
        this.id = id;
    }

    public void onReload(@NotNull BukkitSettings config) {
        this.categories = config.getRegex("(?i)categor(y|ies)").asCollection(new HashSet<>(), OptionalType::asString);
        this.price = config.getIgnoreCase("price").asFloat(0.0f);
        final SettingsItem item = config.getItem(settings -> settings.getRegex("(?i)display(-?item)?"));
        if (item == null) {
            this.display = null;
        } else {
            this.display = item.parse(s -> s.replace("{item_price}", String.valueOf(getPrice()))).build();
        }
        this.online = config.getIgnoreCase("options", "online").asBoolean(false);
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

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public Set<String> getCategories() {
        return categories;
    }

    @NotNull
    public float getPrice() {
        return price;
    }

    @Nullable
    public ItemStack getDisplay() {
        return display;
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

    public void onBuy(@NotNull StoreClient client) {
        client.parser(s -> s.replace("{item_price}", String.valueOf(price)));
        for (StoreAction action : onBuy) {
            action.run(client);
        }
    }

    public void onRecover(@NotNull StoreClient client) {
        if (onRecover == null) {
            onBuy(client);
            return;
        }
        client.parser(s -> s.replace("{item_price}", String.valueOf(price)));
        for (StoreAction action : onRecover) {
            action.run(client);
        }
    }

    public void onRefund(@NotNull StoreClient client) {
        if (onRefund == null) {
            return;
        }
        client.parser(s -> s.replace("{item_price}", String.valueOf(price)));
        for (StoreAction action : onRefund) {
            action.run(client);
        }
    }
}
