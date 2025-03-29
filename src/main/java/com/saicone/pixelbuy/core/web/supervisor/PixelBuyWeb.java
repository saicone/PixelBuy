package com.saicone.pixelbuy.core.web.supervisor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.core.store.StoreItem;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.core.web.WebType;
import com.saicone.pixelbuy.module.hook.PlayerProvider;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * PixelBuyWeb supervisor implementation.
 * This object checks the custom REST API and processes orders.
 *
 */
public class PixelBuyWeb extends WebSupervisor {

    private String baseUrl;
    private URL apiUrl;
    private int delay;
    private int getTask;
    private transient boolean onTask;
    private final Cache<Integer, StoreOrder> cachedOrders = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public PixelBuyWeb(@NotNull String id, @NotNull String group) {
        super(id, group);
    }

    @Override
    public @NotNull WebType getType() {
        return WebType.CUSTOM;
    }

    @Override
    public void onLoad(@NotNull BukkitSettings config) {
        this.baseUrl = config.getRegex("(?i)url|link").asString();
        if (this.baseUrl == null || this.baseUrl.isBlank()) {
            this.apiUrl = null;
            this.delay = -1;
            return;
        }

        String apiPath = config.getRegex("(?i)(api-?)?path").asString("api/orders/{serverId}");
        try {
            this.apiUrl = new URL(parseUrl(this.baseUrl, apiPath));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.delay = config.getRegex("(?i)((delay|check(er)?)-?)?(time|interval|seconds?)").asInt(7) * 20;
    }

    @Override
    public void onStart() {
        if (delay < 1) {
            return;
        }
        getTask = Bukkit.getScheduler().runTaskTimerAsynchronously(PixelBuy.get(), () -> {
            PixelBuy.log(4, "Checking orders...");
            if (onTask) {
                PixelBuy.log(4, "Cannot check orders due task lock");
                return;
            }
            onTask = true;
            try {
                getOrders();
            } catch (Throwable t) {
                PixelBuy.logException(1, t);
            }
            onTask = false;
        }, 200, delay).getTaskId();
    }

    @Override
    public void onClose() {
        if (getTask > 0) {
            Bukkit.getScheduler().cancelTask(getTask);
            getTask = -1;
            onTask = false;
        }
    }

    @Override
    public @Nullable LocalDate getDate(int orderId) {
        final JsonObject json = getOrderJson(orderId);
        return LocalDate.parse(json.get("date").getAsString());
    }

    @Override
    public float getTotal(int orderId) {
        final JsonObject json = getOrderJson(orderId);
        return json.get("price").getAsFloat();
    }

    @Override
    public @Nullable StoreOrder lookupOrder(int orderId, @Nullable String player) {
        final StoreOrder cached = cachedOrders.getIfPresent(orderId);
        if (cached != null) {
            return cached;
        }
        final JsonObject json;
        try {
            json = getOrderJson(orderId);
        } catch (Throwable t) {
            return null;
        }
        try {
            String playerId = player;
            List<String> commands = new ArrayList<>();
            for (JsonElement element : json.getAsJsonArray("items")) {
                final JsonObject item = element.getAsJsonObject();
                commands.add(item.get("key").getAsString());
            }
            if (playerId != null && !commands.isEmpty()) {
                final StoreOrder order = buildOrder(orderId, PlayerProvider.getUniqueId(playerId), commands);
                cachedOrders.put(orderId, order);
                return order;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @NotNull
    public JsonObject getOrderJson(int orderId) {
        return readJson(apiUrl.toString().replace("{orderId}", String.valueOf(orderId)));
    }

    public void getOrders() {
        if (apiUrl == null || apiUrl.toString().isBlank()) {
            return;
        }
        JsonObject response = readJson(apiUrl);
        JsonArray orders = response.getAsJsonArray("orders");
        if (orders == null || orders.isEmpty()) {
            return;
        }

        final List<Integer> processed = new ArrayList<>();
        for (JsonElement element : orders) {
            final JsonObject order = element.getAsJsonObject();
            final int id = order.get("id").getAsInt();
            final List<String> items = new ArrayList<>();
            for (JsonElement item : order.getAsJsonArray("items")) {
                items.add(item.getAsJsonObject().get("key").getAsString());
            }
            if (processOffline(order.get("player").getAsString(), id, items)) {
                processed.add(id);
            }
        }
        if (!processed.isEmpty()) {
            sendOrders(processed);
        }
    }

    public void sendOrders(@NotNull List<Integer> processed) {
        final StringJoiner joiner = new StringJoiner(",", "{\"orders\":[", "]}");
        for (Integer id : processed) {
            joiner.add(String.valueOf(id));
        }
        try {
            HttpsURLConnection con = (HttpsURLConnection) apiUrl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            final byte[] input = joiner.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = con.getOutputStream()) {
                out.write(input, 0, input.length);
            }

            final JsonObject response = readJson(con);
            if (response.get("data") != null) {
                PixelBuy.log(2, response.get("code").getAsString());
            }
            con.getInputStream().close();
            con.disconnect();
        } catch (IOException e) {
            PixelBuy.logException(2, e, "There's an exception while updating the processed orders");
        }
    }
}