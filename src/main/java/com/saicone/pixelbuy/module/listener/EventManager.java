package com.saicone.pixelbuy.module.listener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.saicone.pixelbuy.PixelBuy;

import com.saicone.pixelbuy.api.event.OrderProcessedEvent;
import com.saicone.pixelbuy.util.GsonAdapter;
import com.saicone.pixelbuy.util.Utils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EventManager implements Listener {

    private final PixelBuy pl = PixelBuy.get();

    private final List<RefundedItem> items = new ArrayList<>();

    public EventManager() {
        pl.getServer().getPluginManager().registerEvents(this, pl);
        loadItems();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        pl.getPlayerManager().loadPlayer(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        pl.getPlayerManager().unloadPlayer(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        pl.getPlayerManager().unloadPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProcessOrder(OrderProcessedEvent e) {
        pl.getPlayerManager().processOrder(e.getPlayer(), e.getOrder());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (items.isEmpty()) return;
        ItemStack current = e.getInventory().getItem(e.getSlot());
        if (current != null || !current.getType().toString().contains("AIR")) {
            for (RefundedItem item : items) {
                if (item.getItem().isSimilar(current)) {
                    e.setCancelled(true);
                    if (current.getAmount() > item.getAmount()) {
                        current.setAmount(current.getAmount() - item.getAmount());
                        items.remove(item);
                    } else if (current.getAmount() == item.getAmount()) {
                        e.getInventory().setItem(e.getSlot(), null);
                        items.remove(item);
                    } else {
                        e.getInventory().setItem(e.getSlot(), null);
                        reduceItemAmount(item, current.getAmount());
                    }
                    break;
                }
            }
        }
    }

    public void addItem(ItemStack item, Integer amount) {
        items.add(new RefundedItem(item, amount));
    }

    public void reduceItemAmount(RefundedItem item, Integer amount) {
        List<RefundedItem> items = new ArrayList<>();
        for (RefundedItem it : this.items) {
            if (it.equals(item)) {
                item.reduceAmount(amount);
                items.add(item);
            } else {
                items.add(it);
            }
        }
        this.items.clear();
        this.items.addAll(items);
    }

    public void loadItems() {
        File refunded = new File(pl.getFolderData(), "refunded-items.json");
        if (refunded.exists()) {
            try {
                Reader reader = Files.newBufferedReader(Paths.get(refunded.toString()));
                final Gson gson = new GsonBuilder().disableHtmlEscaping().registerTypeHierarchyAdapter(ConfigurationSerializable.class, new GsonAdapter()).create();
                if (!reader.toString().isEmpty()) {
                    Type listType = new TypeToken<ArrayList<RefundedItem>>(){}.getType();
                    items.addAll(gson.fromJson(reader, listType));
                }
                Utils.info(pl.langString("Plugin.Init.Refunded-Items").replace("%num%", String.valueOf(items.size())));
            } catch (IOException ignored) { }
        }
    }

    public void shut() {
        File refunded = new File(pl.getFolderData(), "refunded-items.json");
        refunded.delete();

        try {
            refunded.createNewFile();
        } catch (IOException ignored) { }

        try {
            FileWriter writer = new FileWriter(refunded);
            final Gson gson = new GsonBuilder().disableHtmlEscaping().registerTypeHierarchyAdapter(ConfigurationSerializable.class, new GsonAdapter()).create();
            String dataString = gson.toJson(items);
            writer.write(dataString);
            writer.flush();
            writer.close();
        } catch (IOException ignored) { }
        items.clear();
        HandlerList.unregisterAll(this);
    }

    private static class RefundedItem {
        private final ItemStack item;
        private Integer amount;

        public RefundedItem(ItemStack item, Integer amount) {
            this.item = item;
            this.amount = amount;
        }

        public ItemStack getItem() {
            return item;
        }

        public Integer getAmount() {
            return amount;
        }

        public void reduceAmount(Integer amount) {
            this.amount = this.amount - amount;
        }
    }
}