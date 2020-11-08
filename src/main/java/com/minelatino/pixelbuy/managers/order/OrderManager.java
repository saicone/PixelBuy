package com.minelatino.pixelbuy.managers.order;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.order.objects.Order;
import com.minelatino.pixelbuy.managers.order.objects.SavedOrders;
import com.minelatino.pixelbuy.managers.order.objects.WebString;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import com.minelatino.pixelbuy.util.Utils;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    private final PixelBuy pl = PixelBuy.get();

    private boolean debug = false;

    private int checker;

    public OrderManager() {
        reload(true);
    }

    public void reload(boolean init) {
        if (!init) Bukkit.getScheduler().cancelTask(checker);
        debug = pl.getFiles().getConfig().getBoolean("Web-Data.Debug");
        checker = Bukkit.getScheduler().runTaskTimerAsynchronously(pl, () -> checkWebData(null), pl.getFiles().getConfig().getInt("Web-Data.Check-Interval") * 20, pl.getFiles().getConfig().getInt("Web-Data.Check-Interval") * 20).getTaskId();
    }

    public URL getURL() throws Exception {
        return new URL(pl.getFiles().getConfig().getString("Web-Data.URL") + "/wp-json/wmc/v1/server/" + pl.getFiles().getConfig().getString("Web-Data.Key"));
    }

    /**
     * Checks WebData String
     */
    public void checkWebData(CommandSender sender) {
        // First of all the plugin will check player data stored on database
        pl.getPlayerManager().processPlayers();

        // Check if plugin is correctly configured
        if (pl.getFiles().getConfig().getString("Web-Data.URL").isEmpty()) {
            if (debug) Utils.info(pl.getFiles().getLang().getString("Debug.WebData.Empty-URL"));
            return;
        }
        if (pl.getFiles().getConfig().getString("Web-Data.Key").isEmpty()) {
            if (debug) Utils.info(pl.getFiles().getLang().getString("Debug.WebData.Empty-Key"));
            return;
        }

        // First debug message
        if (debug) Utils.info(pl.getFiles().getLang().getString("Debug.WebData.Check"));

        // Check URL connection
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(getURL().openStream()));
        } catch (FileNotFoundException e) {
            // Error about connection
            if (debug) Utils.info(e.getMessage().replace(pl.getFiles().getConfig().getString( "Web-Data.Key"), "privateKey"));
            return;
        } catch (Exception e) {
            // Error about bad configured URL
            if (debug) Utils.info(pl.getFiles().getLang().getString("Debug.WebData.Invalid-URL"));
            return;
        }

        // WebData string builder
        StringBuilder buffer = new StringBuilder();
        String line;
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Final checker
        if (buffer.toString().isEmpty()) {
            if (debug) Utils.info(pl.getFiles().getLang().getString("Debug.WebData.Empty-Page"));
        } else {
            processData(sender, buffer.toString());
        }
    }

    /**
     * Read JSON string and process them
     */
    public void processData(CommandSender sender, String webData) {
        // First debug message
        if (debug) Utils.info(pl.getFiles().getLang().getString("Debug.WebData.Check-Data"));

        // Read web data
        Gson gson = new GsonBuilder().create();
        WebString webString = gson.fromJson(webData, WebString.class);

        // Check if Wordpress plugin have no errors
        if (webString.getData() != null) {
            if (debug) Utils.info(webString.getCode());
            return;
        }

        // Create a list of available orders
        List<Order> orderList = webString.getOrders();

        // Check if order list have orders
        if (orderList == null || orderList.isEmpty()) {
            if (debug) Utils.info(pl.getFiles().getLang().getString("Debug.WebData.Empty-Orders"));
            return;
        }

        List<Integer> savedOrders = new ArrayList<>();
        for (Order order : orderList) {
            Player player = Utils.getPlayer(order.getPlayer());
            List<String> cmds = order.getCommands();

            if (player != null) {
                for (String cmd : cmds) {
                    Bukkit.getScheduler().runTaskAsynchronously(pl, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
                }
            } else {
                String p = (pl.getFiles().getConfig().getBoolean("Database.UUID") ? String.valueOf(Bukkit.getOfflinePlayer(order.getPlayer()).getUniqueId()) : order.getPlayer());
                PlayerData playerData = new PlayerData(p, order.getOrderId(), order.getCommands());
                pl.getDatabase().saveData(playerData);
            }
            savedOrders.add(order.getOrderId());
        }
        sendProcessedData(sender, savedOrders);
    }

    /**
     * Sends the processed orders to wordpress.
     */
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
            return;
        }

        // Check if server has a connection
        if (response.body() == null) {
            String sd = "Received empty response from your server, check connections.";
            return;
        }

        // Check any existing errors from Wordpress
        WebString webString = gson.fromJson(response.body().toString(), WebString.class);
        if (webString.getCode() != null) {
            String zd = "Received error when trying to send post data:" + webString.getCode();
        }
    }
}
