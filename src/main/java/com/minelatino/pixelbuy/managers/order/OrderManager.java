package com.minelatino.pixelbuy.managers.order;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import com.minelatino.pixelbuy.util.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class OrderManager {

    private final PixelBuy pl = PixelBuy.get();

    private int checker;

    private boolean on = false;

    public OrderManager() {
        reload(true);
    }

    public void reload(boolean init) {
        if (!init) {
            on = false;
            Bukkit.getScheduler().cancelTask(checker);
        }
        int check = pl.configInt("Web-Data.Check-Interval");
        if (check > 0) {
            checker = Bukkit.getScheduler().runTaskTimerAsynchronously(pl, () -> {
                if (!on) {
                    on = true;
                    checkWebData(null);
                }
            }, check * 20, check * 20).getTaskId();
        }
    }

    public URL getURL() throws Exception {
        return new URL(pl.configString("Web-Data.URL").replace("https:", "http:") + "/wp-json/wmc/v1/server/" + pl.configString("Web-Data.Key"));
    }

    public void checkWebData(CommandSender sender) {
        boolean debug = pl.configBoolean("Web-Data.Debug");

        // Check if plugin is correctly configured
        if (pl.configString("Web-Data.URL").isEmpty()) {
            on = false;
            if (debug) Utils.info(pl.langString("Debug.WebData.Empty-URL"));
            return;
        }
        if (pl.configString("Web-Data.Key").isEmpty()) {
            on = false;
            if (debug) Utils.info(pl.langString("Debug.WebData.Empty-Key"));
            return;
        }

        // First debug message
        if (debug) Utils.info(pl.langString("Debug.WebData.Check-URL"));

        // Check URL connection
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(getURL().openStream()));
        } catch (FileNotFoundException e) {
            // Error about connection
            on = false;
            if (debug) Utils.info(e.getMessage().replace(pl.configString( "Web-Data.Key"), "privateKey"));
            return;
        } catch (Exception e) {
            // Error about bad configured URL
            on = false;
            if (debug) Utils.info(pl.langString("Debug.WebData.Invalid-URL"));
            return;
        }

        // WebData string builder
        StringBuilder buffer = new StringBuilder();
        String line;
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
        } catch (IOException e) {
            on = false;
            e.printStackTrace();
            return;
        }

        // Final checker
        if (buffer.toString().isEmpty()) {
            on = false;
            if (debug) Utils.info(pl.langString("Debug.WebData.Empty-Page"));
        } else {
            processData(sender, buffer.toString(), debug);
        }
    }

    public void processData(CommandSender sender, String webData, boolean debug) {
        // First debug message
        if (debug) Utils.info(pl.langString("Debug.WebData.Check-Data"));

        // Read web data
        Gson gson = new GsonBuilder().create();
        WebString webString = gson.fromJson(webData, WebString.class);

        // Check if Wordpress plugin have no errors
        if (webString.getData() != null) {
            on = false;
            if (debug) Utils.info(webString.getCode());
            return;
        }

        // Create a list of available orders
        List<WebString.Order> webOrderList = webString.getOrders();

        // Check if order list have orders
        if (webOrderList == null || webOrderList.isEmpty()) {
            on = false;
            if (debug) Utils.info(pl.langString("Debug.WebData.Empty-Orders"));
            return;
        }

        List<Integer> savedOrders = new ArrayList<>();
        for (WebString.Order webOrder : webOrderList) {
            Map<String, Byte> items = new HashMap<>();
            webOrder.getItems().forEach(item -> items.put(item, (byte) 1));
            pl.getPlayerManager().processOrder(webOrder.getPlayer(), new PlayerData.Order(webOrder.getOrderId(), items));
            savedOrders.add(webOrder.getOrderId());
        }
        sendProcessedData(sender, savedOrders, debug);
    }

    public void sendProcessedData(CommandSender sender, List<Integer> orders, boolean debug) {
        // Build saved orders data to send
        Gson gson = new Gson();
        SavedOrders savedOrders = new SavedOrders(orders);
        String ordersIds = gson.toJson(savedOrders);

        // Build a request and get the response
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ordersIds);
        Request request;
        Response response;
        try {
            request = new Request.Builder().url(getURL()).post(body).build();
            response = new OkHttpClient().newCall(request).execute();
        } catch (Exception e) {
            on = false;
            return;
        }

        // Check if server has a connection
        if (response.body() == null) {
            on = false;
            if (debug) Utils.info("Received empty response from your server, check connections.");
            return;
        }

        // Check any existing errors from Wordpress
        //WebString webString = gson.fromJson(Objects.requireNonNull(response.body()).toString(), WebString.class);
        //if (webString.getCode() != null) {
        //    if (debug) Utils.info("Received error when trying to send post data:" + webString.getCode());
        //}
        on = false;
    }

    public static class SavedOrders {
        private List<Integer> processedOrders;

        public SavedOrders(List<Integer> processedOrders) {
            this.processedOrders = processedOrders;
        }
    }
}
