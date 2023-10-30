package com.saicone.pixelbuy.core.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.core.store.StoreItem;
import com.saicone.pixelbuy.module.hook.PlayerIdProvider;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
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

    public void onLoad(@NotNull BukkitSettings config) {
    }

    public void onStart() {
    }

    public void onClose() {
    }

    public boolean processOnline(@NotNull UUID uniqueId, int id, @NotNull List<String> items) {
        final StoreOrder order = new StoreOrder(getId(), id, getGroup());
        order.setBuyer(uniqueId);
        for (String item : items) {
            order.addItem(getGroup(), item);
        }
        return PixelBuy.get().getStore().getCheckout().process(order);
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
            final StoreOrder order = new StoreOrder(getId(), id, getGroup());
            order.setBuyer(PlayerIdProvider.getUniqueId(name));
            for (String item : items) {
                order.addItem(getGroup(), item);
            }
            return PixelBuy.get().getStore().getCheckout().process(order);
        }
        return false;
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
}
