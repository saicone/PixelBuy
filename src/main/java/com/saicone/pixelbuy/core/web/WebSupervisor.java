package com.saicone.pixelbuy.core.web;

import com.google.gson.Gson;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.core.store.StoreItem;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class WebSupervisor {

    protected static final String URL = "{url}";
    protected static final String KEY = "{key}";
    protected static final String USERNAME = "{username}";
    protected static final String PASSWORD = "{password}";

    private final String id;
    private final String group;

    protected Gson gson = new Gson();
    private final Map<String, String> secrets = new HashMap<>();

    public WebSupervisor(@NotNull String id, @NotNull String group) {
        this.id = id;
        this.group = group;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getGroup() {
        return group;
    }

    @NotNull
    public abstract WebType getType();

    public void onLoad(@NotNull BukkitSettings config) {
    }

    public void onStart() {
    }

    public void onClose() {
    }

    @NotNull
    public abstract Optional<StoreOrder> lookupOrder(int orderId, @Nullable String player);

    public boolean process(@NotNull String name, @NotNull List<String> items, @NotNull Supplier<StoreOrder> supplier) {
        boolean result = true;
        final Player player = Bukkit.getPlayer(name);
        if (player == null || !player.isOnline()) {
            for (var entry : PixelBuy.get().getStore().getItems().entrySet()) {
                final StoreItem item = entry.getValue();
                if (!items.contains(item.getId())) {
                    continue;
                }
                if (item.isAlwaysSave()) {
                    result = true;
                    break;
                }
                if (item.isOnline()) {
                    result = false;
                }
            }
        }
        if (result) {
            return PixelBuy.get().getStore().getCheckout().process(supplier.get());
        }
        return false;
    }

    protected void clear() {
        secrets.clear();
    }

    @Nullable
    @Contract("!null -> !null")
    protected String addSecret(@Nullable String secret) {
        if (secret == null) {
            return null;
        }
        if (secret.startsWith("file:")) {
            try {
                secret = String.join("", Files.readAllLines(Paths.get(secret.substring(5)))).trim();
            } catch (IOException e) {
                PixelBuy.logException(2, e);
            }
        } else if (secret.startsWith("property:")) {
            secret = System.getProperty(secret.substring(9));
        }
        secrets.put(secret, "*".repeat(secret.length()));
        return secret;
    }

    @Nullable
    @Contract("!null -> !null")
    protected String hideSecrets(@Nullable String s) {
        if (s == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : secrets.entrySet()) {
            s = s.replace(entry.getKey(), entry.getValue());
        }
        return s;
    }

    @Nullable
    @Contract("!null -> !null")
    protected String parseUrl(@Nullable String url) {
        if (url == null) {
            return null;
        }
        if (!url.toLowerCase().startsWith("http")) {
            url = "https://" + url;
        }
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }
}
