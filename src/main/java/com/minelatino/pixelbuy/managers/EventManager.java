package com.minelatino.pixelbuy.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.minelatino.pixelbuy.PixelBuy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
import java.util.Objects;

public class EventManager implements Listener {

    private final PixelBuy pl = PixelBuy.get();

    private final List<ItemStack> items = new ArrayList<>();

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
    public void itemInteract(InventoryClickEvent e) {
        if (items.isEmpty()) return;
        if (items.contains(e.getCurrentItem())) {
            e.setCancelled(true);
            e.getInventory().remove(Objects.requireNonNull(e.getCurrentItem()));
        }
    }

    public void loadItems() {
        File refunded = new File(pl.getFolderData(), "refunded-items.json");
        if (refunded.exists()) {
            try {
                Reader reader = Files.newBufferedReader(Paths.get(refunded.toString()));
                Gson gson = new Gson();
                if (!reader.toString().isEmpty()) {
                    Type itemList = new TypeToken<List<ItemStack>>(){}.getType();
                    items.addAll(gson.fromJson(reader, itemList));
                }
            } catch (IOException ignored) { }
        }
    }

    public void addItem(ItemStack item) {
        items.add(item);
    }

    public void shut() {
        File refunded = new File(pl.getFolderData(), "refunded-items.json");
        if (!refunded.exists()) {
            try {
                refunded.createNewFile();
            } catch (IOException ignored) { }
        }
        try {
            FileWriter writer = new FileWriter(refunded);
            String dataString = new Gson().toJson(items);
            writer.write(dataString);
            writer.flush();
            writer.close();
        } catch (IOException ignored) { }
        items.clear();
        HandlerList.unregisterAll(this);
    }
}