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

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private URL url;
    private int delay;

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
        String url = config.getRegex("(?i)url|link").asString();
        if (url == null || url.isBlank()) {
            this.url = null;
            this.delay = -1;
            return;
        }
        if (!url.toLowerCase().startsWith("http")) {
            url = "https://" + url;
        }
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        String path = config.getRegex("(?i)(api-?)?path").asString("wp-json/wmc/v1/server/{key}");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        url = url + path;
        final String key = config.getRegex("(?i)(api-?)?key").asString();
        if (key != null) {
            url = url.replace("{key}", key);
        }
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.delay = config.getRegex("(?i)((delay|check(er)?)-?)?(time|interval|seconds?)").asInt(7);
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

    public void getOrders() {
        if (url == null || url.toString().isBlank()) {
            return;
        }
        final JsonObject json = readJson(url);
        if (json.get("data") != null) {
            PixelBuy.log(2, json.get("code").getAsString());
            return;
        }

        final JsonArray orders = json.getAsJsonArray("orders");
        if (orders == null || orders.isEmpty()) {
            return;
        }

        final List<Integer> processed = new ArrayList<>();
        Bukkit.getScheduler().runTask(PixelBuy.get(), () -> {
            for (JsonElement element : orders) {
                final JsonObject order = element.getAsJsonObject();
                final int id = order.get("order_id").getAsInt();
                final List<String> items = new ArrayList<>();
                for (JsonElement command : order.getAsJsonArray("commands")) {
                    for (String item : command.getAsString().split(",")) {
                        items.add(item.trim());
                    }
                }
                if (process(order.get("player").getAsString(), id, items)) {
                    processed.add(id);
                }
            }
            if (!processed.isEmpty()) {
                Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> sendOrders(processed));
            }
        });
    }

    public void sendOrders(@NotNull List<Integer> processed) {
        final StringJoiner joiner = new StringJoiner(",", "{\"processedOrders\":[", "]}");
        for (Integer id : processed) {
            joiner.add(String.valueOf(id));
        }
        try {
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
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
