package com.saicone.pixelbuy.core.web.supervisor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.core.store.StoreItem;
import com.saicone.pixelbuy.core.web.WebConnection;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.core.web.WebType;
import com.saicone.pixelbuy.core.web.connection.RestConnection;
import com.saicone.pixelbuy.core.web.object.WooCommerceOrder;
import com.saicone.pixelbuy.core.web.object.WordpressError;
import com.saicone.pixelbuy.module.hook.PlayerProvider;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * WooMinecraft supervisor implementation.<br>
 * This object check WordPress site API with WooMinecraft plugin installed,
 * and process every command separated by comma as store items.
 *
 * @author Rubenicos
 */
public class WooMinecraftWeb extends WebSupervisor {

    private static final String WOO_MINECRAFT_SERVER = "{url}/wp-json/wmc/v1/server/{password}";
    private static final String WOO_COMMERCE_ORDER = "{url}/wp-json/wc/v3/orders/{key}";
    private static final String WOO_COMMERCE_PRODUCT = "{url}/wp-json/wc/v3/products/{key}";

    private static final String CONSUMER_KEY = "consumer_key";
    private static final String CONSUMER_SECRET = "consumer_secret";
    private static final String PLAYER_KEY = "player_id";

    // WooMinecraft
    private WebConnection serverConnection;
    // WooCommerce
    private String metaKey;
    private RestConnection<?> orderConnection;
    private RestConnection<?> productConnection;

    private int taskDelay;
    private BukkitTask task;
    private transient boolean onTask;
    private final Cache<Integer, StoreOrder> cachedOrders = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public WooMinecraftWeb(@NotNull String id, @NotNull String group) {
        super(id, group);
    }

    @Override
    public @NotNull WebType getType() {
        return WebType.WOO_MINECRAFT;
    }

    @Override
    public void onLoad(@NotNull BukkitSettings config) {
        clear();

        final String url = parseUrl(config.getRegex("(?i)url|link").asString());
        if (url == null || url.isBlank()) {
            this.serverConnection = null;
            this.orderConnection = null;
            this.productConnection = null;
            this.taskDelay = -1;
            return;
        }

        String serverUrl = config.getRegex("(?i)format", "(?i)server(-?url)?").asString(WOO_MINECRAFT_SERVER);
        serverUrl = serverUrl.replace(URL, url);
        final String password = addSecret(config.getRegex("(?i)(api-?)?key").asString());
        if (password != null) {
            serverUrl = serverUrl.replace(PASSWORD, password);
        }
        this.serverConnection = RestConnection.params(serverUrl);

        this.metaKey = "_wmc_commands_" + password;

        final String consumerKey = addSecret(config.getRegex("(?i)woocommerce", "(?i)consumer-?key").asString());
        final String consumerSecret = addSecret(config.getRegex("(?i)woocommerce", "(?i)consumer-?secret").asString());
        if (consumerKey == null || consumerSecret == null) {
            this.orderConnection = null;
            this.productConnection = null;
        } else {
            final String orderUrl = config.getRegex("(?i)format", "(?i)order(-?url)?").asString(WOO_COMMERCE_ORDER).replace(URL, url);
            final String productUrl = config.getRegex("(?i)format", "(?i)product(-?url)?").asString(WOO_COMMERCE_PRODUCT).replace(URL, url);

            switch (RestConnection.Type.of(config.getRegex("(?i)woocommerce", "(?i)auth(entication)?(-?(method|type)?)").asString()).orElse(RestConnection.Type.PARAMS)) {
                case PARAMS:
                    this.orderConnection = RestConnection.params(orderUrl,
                            CONSUMER_KEY, consumerKey,
                            CONSUMER_SECRET, consumerSecret
                    );
                    this.productConnection = RestConnection.params(productUrl,
                            CONSUMER_KEY, consumerKey,
                            CONSUMER_SECRET, consumerSecret
                    );
                    break;
                case BASIC:
                    this.orderConnection = RestConnection.basic(orderUrl, consumerKey + ":" + consumerSecret);
                    this.productConnection = RestConnection.basic(productUrl, consumerKey + ":" + consumerSecret);
                    break;
                default:
                    break;
            }
            this.orderConnection.cache(5, TimeUnit.MINUTES);
            this.productConnection.cache(5, TimeUnit.MINUTES);
        }

        this.taskDelay = config.getRegex("(?i)((delay|check(er)?)-?)?(time|interval|seconds?)").asInt(7) * 20;
    }

    @Override
    public void onStart() {
        if (taskDelay < 1) {
            return;
        }
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(PixelBuy.get(), () -> {
            PixelBuy.log(4, "Checking orders...");
            if (onTask) {
                PixelBuy.log(4, "Cannot check orders due task lock");
                return;
            }
            onTask = true;
            try {
                processOrders();
            } catch (Throwable t) {
                PixelBuy.logException(1, t);
            }
            onTask = false;
        }, 200, taskDelay);
    }

    @Override
    public void onClose() {
        if (task != null) {
            task.cancel();
            onTask = false;
        }
    }

    @NotNull
    public Optional<WooCommerceOrder> getOrder(int orderId) {
        if (this.orderConnection == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(this.orderConnection.fetch(WooCommerceOrder.class, KEY, orderId));
        } catch (IOException e) {
            throw new RuntimeException("Cannot get order " + orderId + " from site connection", e);
        }
    }

    public float getPrice(@NotNull Object product) {
        if (this.productConnection == null) {
            return 0.0f;
        }
        try {
            final JsonObject json = this.productConnection.fetch(JsonObject.class, KEY, product);
            if (json == null) {
                return 0.0f;
            }
            return json.get("price").getAsFloat();
        } catch (IOException e) {
            throw new RuntimeException("Cannot get product " + product + " from site connection", e);
        }
    }

    @Override
    public @NotNull Optional<StoreOrder> lookupOrder(int orderId, @Nullable String player) {
        final StoreOrder cached = cachedOrders.getIfPresent(orderId);
        if (cached != null) {
            return Optional.of(cached);
        }
        return getOrder(orderId).map(order -> {
            String playerId = player;
            List<String> commands = null;

            for (WooCommerceOrder.MetaData meta : order.metaData()) {
                if (playerId != null && commands != null) {
                    break;
                }
                if (meta.key().equalsIgnoreCase(PLAYER_KEY)) {
                    playerId = String.valueOf(meta.value());
                } else if (meta.key().equals(this.metaKey)) {
                    commands = new ArrayList<>();
                    if (meta.value() instanceof Iterable) {
                        for (Object element : (Iterable<?>) meta.value()) {
                            commands.add(String.valueOf(element));
                        }
                    } else {
                        commands.add(String.valueOf(meta.value()));
                    }
                }
            }

            if (playerId != null && commands == null) {
                for (WooCommerceOrder.LineItem item : order.lineItems()) {
                    final StoreItem storeItem = PixelBuy.get().getStore().getItem(it -> {
                        Object id = it.getProduct(getId());
                        return id != null && id.equals(item.productId());
                    });
                    if (storeItem != null) {
                        if (commands == null) {
                            commands = new ArrayList<>();
                        }
                        commands.add(storeItem.getId());
                    }
                }
            }

            if (playerId != null && commands != null) {
                final StoreOrder storeOrder = new Order(orderId, playerId, commands).asStoreOrder(this, order);
                cachedOrders.put(orderId, storeOrder);
                return storeOrder;
            }
            return null;
        });
    }

    @Override
    public void markAsCompleted(@NotNull Integer... orderIds) throws IOException {
        final List<String> orders = new ArrayList<>();
        for (Integer orderId : orderIds) {
            orders.add(String.valueOf(orderId));
        }
        sendOrders(orders);
    }

    public void processOrders() throws IOException {
        if (serverConnection == null) {
            return;
        }

        final Server server = serverConnection.fetch(Server.class);
        if (server == null) {
            return;
        }
        if (server.isError()) {
            server.throwError(this::hideSecrets);
            return;
        }

        if (PixelBuy.get().getLang().getLogLevel() >= 4) {
            PixelBuy.log(4, "Last orders data: " + server);
        }

        final List<String> processed = new ArrayList<>();
        for (Order order : server.orders()) {
            final List<String> items = new ArrayList<>();
            for (String command : order.commands()) {
                for (String item : command.split(",")) {
                    items.add(item.trim());
                }
            }
            if (process(order.player(), items, () -> order.asStoreOrder(this))) {
                processed.add(String.valueOf(order.id()));
            }
        }
        if (!processed.isEmpty()) {
            sendOrders(processed);
        }
    }

    public void sendOrders(@NotNull List<String> processed) throws IOException {
        final WordpressError response = serverConnection.send(WordpressError.class, new Server().processedOrders(processed));
        if (response != null && response.isError()) {
            response.throwError(this::hideSecrets);
        }
    }

    public static class Server extends WordpressError {

        private List<Order> orders = new ArrayList<>();
        private List<String> processedOrders;

        public List<Order> orders() {
            return orders;
        }

        public List<String> processedOrders() {
            return processedOrders;
        }

        @NotNull
        @Contract("_ -> this")
        public Server processedOrders(@NotNull List<String> processedOrders) {
            this.processedOrders = processedOrders;
            return this;
        }

        @Override
        public String toString() {
            return "Server{" +
                    "orders=" + orders +
                    ", processedOrders=" + processedOrders +
                    '}';
        }
    }

    public static class Order {

        @SerializedName("order_id")
        private Integer id;
        private String player;
        private List<String> commands;

        public Order() {
            this.commands = new ArrayList<>();
        }

        public Order(@NotNull Integer id, @NotNull String player, @NotNull List<String> commands) {
            this.id = id;
            this.player = player;
            this.commands = commands;
        }

        private transient List<String> items;

        public Integer id() {
            return id;
        }

        public String player() {
            return player;
        }

        public List<String> commands() {
            return commands;
        }

        @NotNull
        public List<String> items() {
            if (this.items == null) {
                this.items = new ArrayList<>();
                for (String command : this.commands) {
                    for (String item : command.split(",")) {
                        this.items.add(item.trim());
                    }
                }
            }
            return this.items;
        }

        @NotNull
        public StoreOrder asStoreOrder(@NotNull WooMinecraftWeb web) {
            return asStoreOrder(web, web.getOrder(id).orElse(null));
        }

        @NotNull
        public StoreOrder asStoreOrder(@NotNull WooMinecraftWeb web, @Nullable WooCommerceOrder wOrder) {
            final StoreOrder order = new StoreOrder(web.getId(), id, web.getGroup());
            order.setBuyer(PlayerProvider.getUniqueId(player));
            if (wOrder != null) {
                order.setDate(LocalDate.parse(wOrder.dateCreated()));
            }

            for (String item : items()) {
                int amount = 1;
                float price = 0.0f;

                final StoreItem storeItem = PixelBuy.get().getStore().getItem(item);
                if (storeItem != null) {
                    price = storeItem.getPrice();

                    boolean found = false;
                    final Object product = storeItem.getProduct(web.getId());
                    if (wOrder != null && product != null) {
                        for (WooCommerceOrder.LineItem lineItem : wOrder.lineItems()) {
                            if (lineItem.productId().equals(product)) {
                                amount = lineItem.quantity();
                                price = Float.parseFloat(lineItem.total());
                                found = true;
                            }
                        }
                    }

                    if (!found && product != null) {
                        price = web.getPrice(product);
                    }
                }

                order.addItem(web.getGroup(), item, price).amount(amount);
            }

            return order;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "id=" + id +
                    ", player='" + player + '\'' +
                    ", commands=" + commands +
                    '}';
        }
    }
}
