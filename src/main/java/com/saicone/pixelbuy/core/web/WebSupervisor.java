package com.saicone.pixelbuy.core.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.core.store.StoreItem;
import com.saicone.pixelbuy.module.hook.PlayerProvider;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public abstract class WebSupervisor {

    private final String id;
    private final String group;

    public WebSupervisor(@NotNull String id, @NotNull String group) {
        this.id = id;
        this.group = group;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getGroup() {
        return group;
    }

    @NotNull
    public abstract WebType getType();

    @Nullable
    public LocalDate getDate(int orderId) {
        return null;
    }

    public float getTotal(int orderId) {
        return Float.MIN_VALUE;
    }

    public float getTotal(int orderId, int itemId) {
        return Float.MIN_VALUE;
    }

    public float getPrice(int itemId) {
        return Float.MIN_VALUE;
    }

    @Nullable
    public StoreOrder lookupOrder(int orderId) {
        return null;
    }

    public void onLoad(@NotNull BukkitSettings config) {
    }

    public void onStart() {
    }

    public void onClose() {
    }

    public boolean processOnline(@NotNull UUID uniqueId, int id, @NotNull List<String> items) {
        return PixelBuy.get().getStore().getCheckout().process(buildOrder(id, uniqueId, items));
    }

    public boolean processOffline(@NotNull String name, int id, @NotNull List<String> items) {
        boolean result = true;
        for (var entry : PixelBuy.get().getStore().getItems().entrySet()) {
            final StoreItem item = entry.getValue();
            if (!items.contains(item.getId())) {
                continue;
            }
            if (item.isAlwaysRun()) {
                result = true;
                break;
            }
            if (item.isOnline()) {
                result = false;
            }
        }
        if (result) {
            return PixelBuy.get().getStore().getCheckout().process(buildOrder(id, PlayerProvider.getUniqueId(name), items));
        }
        return false;
    }

    @NotNull
    public StoreOrder buildOrder(int id, @NotNull UUID buyer, @NotNull List<String> items) {
        final StoreOrder order = new StoreOrder(getId(), id, getGroup());
        order.setBuyer(buyer);
        for (String item : items) {
            final StoreItem storeItem = PixelBuy.get().getStore().getItem(item);
            final Integer itemId = storeItem != null ? storeItem.getPriceElement(getId()) : null;
            if (itemId != null) {
                try {
                    final float total = getTotal(id, itemId);
                    if (total != Float.MIN_VALUE) {
                        order.addItem(getGroup(), item, total < 0.0f ? 0.0001f : total);
                        continue;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            order.addItem(getGroup(), item);
        }
        return order;
    }

    @NotNull
    protected JsonObject readJson(@NotNull URL url) {
        try {
            return readJson(url.openConnection());
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse the URL as json data", e);
        }
    }

    @NotNull
    protected JsonObject readJson(@NotNull URLConnection con) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(con.getInputStream()), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @NotNull
    protected String parseUrl(@NotNull String baseUrl, @NotNull String path) {
        if (!baseUrl.toLowerCase().startsWith("http")) {
            baseUrl = "https://" + baseUrl;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUrl + path;
    }
}
