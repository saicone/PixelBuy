package com.minelatino.pixelbuy.module.store;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StoreConnection {

    private String page;
    private String key;
    private URL url;
    private final Gson gson;

    public StoreConnection(String page, String key) {
        this.page = page;
        this.key = key;
        gson = new Gson();
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean setup() {
        if (page == null || page.isEmpty()) {
            // Debug: Empty page
            return false;
        }

        if (key == null || key.isEmpty()) {
            // Debug: Empty key
            return false;
        }

        try {
            url = new URL(page.replace("https:", "http:") + "/wp-json/wmc/v1/server/" + key);
            return true;
        } catch (MalformedURLException e) {
            // Debug: Bad url
            e.printStackTrace();
            return false;
        }
    }

    public boolean check() {
        // Debug: Starting to check...
        String content;
        try (InputStream in = url.openStream(); BufferedInputStream buff = new BufferedInputStream(in)) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = buff.read(buf)) > 0) {
                stream.write(buf, 0, len);
            }
            content = new String(stream.toByteArray(), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            // Debug: Bad connection (maybe Cloudflare)
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (content.isEmpty()) {
            // Debug: Empty page
            return true;
        }

        JsonObject object = gson.fromJson(content, JsonObject.class);
        if (object.getAsJsonObject("data") != null) {
            // Debug: object.get("code").getAsString();
            return false;
        }

        List<StoreOrder> orders = new ArrayList<>();
        object.getAsJsonArray("orders").forEach(obj -> orders.add(gson.fromJson(obj.toString(), StoreOrder.class)));

        if (orders.isEmpty()) {
            // Debug: No orders
            return true;
        }

        List<Integer> checked = new ArrayList<>();
        orders.forEach(order -> {
            // Make the check
            checked.add(order.getOrderId());
        });

        JsonObject saved = new JsonObject();
        saved.add("processedOrders", gson.toJsonTree(checked));

        send(gson.toJson(saved));
        return true;
    }

    public boolean send(String data) {
        boolean result = false;
        try {
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.addRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            OutputStream out = con.getOutputStream();
            out.write(input, 0, input.length);
            out.flush();
            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }

            if (!response.toString().isEmpty()) {
                // Debug: Error while connect
                JsonObject object = gson.fromJson(response.toString(), JsonObject.class);
                if (object.getAsJsonObject("data") != null) {
                    // Debug: object.get("code").getAsString();
                }
            } else {
                result = true;
            }

            con.getInputStream().close();
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }
}
