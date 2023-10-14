package com.saicone.pixelbuy.core.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreUser;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class WebSupervisor {

    private final String id;

    public WebSupervisor(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public abstract WebType getType();

    public void onLoad(@NotNull BukkitSettings config) {
    }

    public void onStart() {
    }

    public void onClose() {
    }

    public boolean process(@NotNull String player, int order, @NotNull List<String> items) {
        final Map<String, Byte> map = new HashMap<>();
        for (String item : items) {
            map.put(item, (byte) 1);
        }
        return PixelBuy.get().getUserCore().processOrder(player, new StoreUser.Order(order, map), true);
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
