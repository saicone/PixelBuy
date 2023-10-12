package com.saicone.pixelbuy.core.web;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.event.OrderProcessedEvent;
import com.saicone.pixelbuy.api.object.StoreUser;

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

public class WebSupervisor {

    private final PixelBuy pl = PixelBuy.get();

    private int checker = 0;

    private boolean on = false;

    public WebSupervisor() {
        reload(true);
    }

    public void reload(boolean init) {
        on = false;
        if (checker > 0 && !init) Bukkit.getScheduler().cancelTask(checker);
        int check = PixelBuy.settings().getInt("Web-Data.Check-Interval", 7);
        if (check >= 1) {
            checker = Bukkit.getScheduler().runTaskTimerAsynchronously(pl, () -> {
                if (!on) {
                    on = true;
                    checkWebData(null);
                }
            }, check * 20L, check * 20L).getTaskId();
        }
    }

    public void shut() {
        Bukkit.getScheduler().cancelTask(checker);
    }

    public URL getURL() throws Exception {
        return new URL(PixelBuy.settings().getString("Web-Data.URL", "") + "/wp-json/wmc/v1/server/" + PixelBuy.settings().getString("Web-Data.Key", ""));
    }

    public void checkWebData(CommandSender sender) {
        // Check if plugin is correctly configured
        if (PixelBuy.settings().getString("Web-Data.URL", "").isEmpty()) {
            on = false;
            PixelBuy.log(2, "The URL is empty");
            return;
        }
        if (PixelBuy.settings().getString("Web-Data.Key", "").isEmpty()) {
            on = false;
            PixelBuy.log(2, "The Key is empty");
            return;
        }

        // First debug message
        PixelBuy.log(4, "Checking purchase data...");

        // Check URL connection
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(getURL().openStream()));
        } catch (FileNotFoundException e) {
            // Error about connection
            on = false;
            PixelBuy.log(2, e.getMessage().replace(PixelBuy.settings().getString( "Web-Data.Key", ""), "privateKey"));
            return;
        } catch (Exception e) {
            // Error about bad configured URL
            on = false;
            PixelBuy.log(2, "The URL is invalid");
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
            PixelBuy.log(2, "The page with purchase orders is empty");
        } else {
            processData(sender, buffer.toString());
        }
    }

    public void processData(CommandSender sender, String webData) {
        // First debug message
        PixelBuy.log(4, "Reviewing pending purchase orders...");

        // Read web data
        Gson gson = new GsonBuilder().create();
        WebString webString = gson.fromJson(webData, WebString.class);

        // Check if Wordpress plugin have no errors
        if (webString.getData() != null) {
            on = false;
            PixelBuy.log(2, webString.getCode());
            return;
        }

        // Create a list of available orders
        List<WebString.Order> webOrderList = webString.getOrders();

        // Check if order list have orders
        if (webOrderList == null || webOrderList.isEmpty()) {
            on = false;
            PixelBuy.log(4, "There are no purchase orders to process");
            return;
        }

        List<Integer> savedOrders = new ArrayList<>();
        for (WebString.Order webOrder : webOrderList) {
            Map<String, Byte> items = new HashMap<>();
            webOrder.getItems().forEach(item -> {
                for (String it : item.split(",")) {
                    items.put(it, (byte) 1);
                }
            });
            Bukkit.getScheduler().runTask(pl, () -> {
                OrderProcessedEvent event = new OrderProcessedEvent(webOrder.getPlayer(), new StoreUser.Order(webOrder.getOrderId(), items));
                Bukkit.getPluginManager().callEvent(event);
            });
            savedOrders.add(webOrder.getOrderId());
        }
        sendProcessedData(sender, savedOrders);
    }

    public void sendProcessedData(CommandSender sender, List<Integer> orders) {
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
            PixelBuy.log(2, "Received empty response from your server, check connections.");
            return;
        }

        // Check any existing errors from Wordpress
        //WebString webString = gson.fromJson(Objects.requireNonNull(response.body()).toString(), WebString.class);
        //if (webString.getCode() != null) {
        //    PixelBuy.log(2, "Received error when trying to send post data:" + webString.getCode());
        //}
        on = false;
    }

    public static class SavedOrders {
        private final List<Integer> processedOrders;

        public SavedOrders(List<Integer> processedOrders) {
            this.processedOrders = processedOrders;
        }

        public List<Integer> getOrders() {
            return processedOrders;
        }
    }
}
