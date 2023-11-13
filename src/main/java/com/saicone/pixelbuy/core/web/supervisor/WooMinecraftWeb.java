package com.saicone.pixelbuy.core.web.supervisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.core.web.WebType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * WooMinecraft supervisor implementation.<br>
 * This object check WordPress site API with WooMinecraft plugin installed,
 * and process every command separated by comma as store items.<br>
 * <br>
 * <h3>Structure</h3>
 * code = {@code String}<br>
 * message = {@code String}<br>
 * data = {<br>
 * &emsp;status = {@code Integer}<br>
 * }<br>
 * orders = [
 * {<br>
 * &emsp;player = {@code String}<br>
 * &emsp;order_id = {@code Integer}<br>
 * &emsp;commands = {@code List<String>}<br>
 * }
 * ]<br>
 *
 * @author Rubenicos
 */
public class WooMinecraftWeb extends WebSupervisor {

    private String baseUrl;
    private URL wmcUrl;
    private int delay;
    private String wcUrl;

    private int getTask;
    private boolean onTask;

    public WooMinecraftWeb(@NotNull String id, @NotNull String group) {
        super(id, group);
    }

    @Override
    public @NotNull WebType getType() {
        return WebType.WOO_MINECRAFT;
    }

    @Override
    public void onLoad(@NotNull BukkitSettings config) {
        this.baseUrl = config.getRegex("(?i)url|link").asString();
        if (this.baseUrl == null || this.baseUrl.isBlank()) {
            this.wmcUrl = null;
            this.wcUrl = null;
            this.delay = -1;
            return;
        }

        String wmcPath = config.getRegex("(?i)(api-?)?path").asString("wp-json/wmc/v1/server/{key}");
        final String key = config.getRegex("(?i)(api-?)?key").asString();
        if (key != null) {
            wmcPath = wmcPath.replace("{key}", key);
        }
        try {
            this.wmcUrl = new URL(parseUrl(this.baseUrl, wmcPath));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.delay = config.getRegex("(?i)((delay|check(er)?)-?)?(time|interval|seconds?)").asInt(7);

        String wcPath = config.getRegex("(?i)woocommerce", "(?i)(api-?)?path").asString("wp-json/wc/v3/{type}/{id}?consumer_key={key}&consumer_secret={secret}");
        final String consumerKey = config.getRegex("(?i)woocommerce", "(?i)consumer-?key").asString();
        final String consumerSecret = config.getRegex("(?i)woocommerce", "(?i)consumer-?secret").asString();
        if (consumerKey == null || consumerSecret == null) {
            this.wcUrl = null;
        } else {
            wcPath = wcPath.replace("{key}", consumerKey).replace("{secret}", consumerSecret);
            this.wcUrl = parseUrl(this.baseUrl, wcPath);
        }
    }

    @Override
    public void onStart() {
        if (delay < 1) {
            return;
        }
        getTask = Bukkit.getScheduler().runTaskTimerAsynchronously(PixelBuy.get(), () -> {
            if (onTask) {
                return;
            }
            onTask = true;
            try {
                getOrders();
            } catch (Throwable t) {
                PixelBuy.logException(1, t);
            }
            onTask = false;
        }, delay, delay).getTaskId();
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
        if (wcUrl == null) {
            return super.getDate(orderId);
        }
        final JsonObject json = getOrderJson(orderId);
        return LocalDate.parse(json.get("date_created").getAsString());
    }

    @Override
    public float getTotal(int orderId) {
        if (wcUrl == null) {
            return super.getTotal(orderId);
        }
        final JsonObject json = getOrderJson(orderId);
        return json.get("total").getAsFloat();
    }

    @Override
    public float getTotal(int orderId, int itemId) {
        if (wcUrl == null) {
            return super.getTotal(orderId, itemId);
        }
        final JsonObject json = getOrderJson(orderId);
        for (JsonElement element : json.getAsJsonArray("line_items")) {
            final JsonObject item = element.getAsJsonObject();
            if (item.get("product_id").getAsInt() == itemId) {
                return item.get("total").getAsFloat();
            }
        }
        return super.getTotal(orderId, itemId);
    }

    @Override
    public float getPrice(int itemId) {
        if (wcUrl == null) {
            return super.getPrice(itemId);
        }
        final URL url;
        try {
            url = new URL(wcUrl.replace("{type}", "products").replace("{id}", String.valueOf(itemId)));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The item '" + itemId + "' json cannot be retrieved", e);
        }
        final JsonObject json = readJson(url);
        return json.get("price").getAsFloat();
    }

    @NotNull
    public JsonObject getOrderJson(int orderId) {
        final URL url;
        try {
            url = new URL(wcUrl.replace("{type}", "orders").replace("{id}", String.valueOf(orderId)));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The order '" + orderId + "' json cannot be retrieved", e);
        }
        return readJson(url);
    }

    public void getOrders() {
        if (wmcUrl == null || wmcUrl.toString().isBlank()) {
            return;
        }
        final JsonObject json = readJson(wmcUrl);
        if (json.get("data") != null) {
            PixelBuy.log(2, json.get("code").getAsString());
            return;
        }

        final JsonArray orders = json.getAsJsonArray("orders");
        if (orders == null || orders.isEmpty()) {
            return;
        }

        final List<Integer> processed = new ArrayList<>();
        for (JsonElement element : orders) {
            final JsonObject order = element.getAsJsonObject();
            final int id = order.get("order_id").getAsInt();
            final List<String> items = new ArrayList<>();
            for (JsonElement command : order.getAsJsonArray("commands")) {
                for (String item : command.getAsString().split(",")) {
                    items.add(item.trim());
                }
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
        final StringJoiner joiner = new StringJoiner(",", "{\"processedOrders\":[", "]}");
        for (Integer id : processed) {
            joiner.add(String.valueOf(id));
        }
        try {
            HttpsURLConnection con = (HttpsURLConnection) wmcUrl.openConnection();
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
