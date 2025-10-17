package com.saicone.pixelbuy.core.web.supervisor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.annotations.SerializedName;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.core.web.WebConnection;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.core.web.WebType;
import com.saicone.pixelbuy.core.web.connection.RestConnection;
import com.saicone.pixelbuy.module.hook.PlayerProvider;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class PixelBuyWeb extends WebSupervisor {

    private static final String SERVER_FORMAT = "{url}/api/server/{key}";
    private static final String ORDER_FORMAT = "{url}/api/order/{key}";
    private static final String PROPERTY = "secret";

    private WebConnection serverConnection;
    private WebConnection orderConnection;

    private boolean detectDelay;
    private int taskDelay;
    private BukkitTask task;
    private final Cache<Integer, StoreOrder> cachedOrders = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public PixelBuyWeb(@NotNull String id, @NotNull String group) {
        super(id, group);
    }

    @Override
    public @NotNull WebType getType() {
        return WebType.PIXELBUY;
    }

    @Override
    public void onLoad(@NotNull BukkitSettings config) {
        clear();

        final String url = parseUrl(config.getRegex("(?i)url|link").asString());
        if (url == null || url.isBlank()) {
            this.serverConnection = null;
            this.orderConnection = null;
            this.taskDelay = -1;
            return;
        }

        final String serverUrl = config.getRegex("(?i)format", "(?i)server(-?url)?").asString(SERVER_FORMAT).replace(URL, url);
        final String orderUrl = config.getRegex("(?i)format", "(?i)order(-?url)?").asString(ORDER_FORMAT).replace(URL, url);

        final String property = config.getRegex("(?i)rest(-?api)?", "(?i)property").asString(PROPERTY);
        final String secret = addSecret(config.getRegex("(?i)rest(-?api)?", "(?i)secret(-?key)?").asString(""));
        if (secret.isBlank()) {
            this.serverConnection = null;
            this.orderConnection = null;
            this.taskDelay = -1;
            return;
        } else {
            switch (RestConnection.Type.of(config.getRegex("(?i)rest(-?api)?", "(?i)auth(entication)?(-?(method|type)?)").asString()).orElse(RestConnection.Type.PARAMS)) {
                case PARAMS:
                    this.serverConnection = RestConnection.params(serverUrl, property, secret);
                    this.orderConnection = RestConnection.params(orderUrl, property, secret);
                    break;
                case HEADER:
                    this.serverConnection = RestConnection.header(serverUrl, property, secret);
                    this.orderConnection = RestConnection.header(orderUrl, property, secret);
                    break;
                case BASIC:
                    this.serverConnection = RestConnection.basic(serverUrl, secret);
                    this.orderConnection = RestConnection.basic(orderUrl, secret);
                    break;
                default:
                    break;
            }
        }

        String taskDelay = config.getRegex("(?i)rest(-?api)?", "(?i)((delay|check(er)?)-?)?(time|interval|seconds?)").asString("30");
        if (taskDelay.equalsIgnoreCase("DETECT")) {
            this.detectDelay = true;
            taskDelay = "10"; // 10 seconds warmup
        } else {
            this.detectDelay = false;
        }
        this.taskDelay = Integer.parseInt(taskDelay) * 20;
    }

    @Override
    public void onStart() {
        initTask();
    }

    private void initTask() {
        this.task = Bukkit.getScheduler().runTaskLaterAsynchronously(PixelBuy.get(), () -> {
            try {
                int next = (int) processOrders();
                if (next > 0 && this.detectDelay) {
                    this.taskDelay = next * 20;
                }
            } catch (Throwable t) {
                PixelBuy.logException(1, t);
            }
            initTask();
        }, this.taskDelay);
    }

    @Override
    public void onClose() {
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public @NotNull Optional<StoreOrder> lookupOrder(int orderId, @Nullable String player) {
        final StoreOrder cached = cachedOrders.getIfPresent(orderId);
        if (cached != null) {
            return Optional.of(cached);
        }
        try {
            return Optional.ofNullable(orderConnection.fetch(Order.class)).map(order -> {
                final StoreOrder storeOrder = order.asStoreOrder(getId(), getGroup());
                cachedOrders.put(orderId, storeOrder);
                return storeOrder;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markAsCompleted(@NotNull Integer... orderIds) throws IOException {
        final List<Integer> orders = new ArrayList<>();
        Collections.addAll(orders, orderIds);
        sendOrders(orders);
    }

    public long processOrders() throws IOException {
        if (serverConnection == null) {
            return -1;
        }

        final Server server = serverConnection.fetch(Server.class);
        if (server == null) {
            return -1;
        }
        if (server.isError()) {
            server.throwError(this::hideSecrets);
            return -1;
        }

        if (PixelBuy.get().getLang().getLogLevel() >= 4) {
            PixelBuy.log(4, "Last orders data: " + server);
        }

        final List<Integer> orders = new ArrayList<>();
        for (Order order : server.pendingOrders()) {
            if (process(order.playerName(), order.items().stream().map(Item::id).collect(Collectors.toList()), () -> order.asStoreOrder(getId(), getGroup()))) {
                orders.add(order.id());
            }
        }

        if (!orders.isEmpty()) {
            sendOrders(orders);
        }

        return server.nextCheck();
    }

    public void sendOrders(@NotNull List<Integer> orders) throws IOException {
        final Error response = serverConnection.send(Error.class, new Update(orders));
        if (response != null && response.isError()) {
            response.throwError(this::hideSecrets);
        }
    }

    public static class Error {

        private String error;
        private String message;
        private Integer status;

        public boolean isError() {
            return error != null;
        }

        public String error() {
            return error;
        }

        public String message() {
            return message;
        }

        public Integer status() {
            return status;
        }

        public void throwError(@NotNull UnaryOperator<String> filter) throws IOException {
            String filtered = message;
            if (filtered != null) {
                filtered = filter.apply(filtered);
            }
            throw new IOException("[" + error + "/" + status + "]: " + filtered);
        }
    }

    public static class Server extends Error {

        @SerializedName("pending_orders")
        private List<Order> pendingOrders = new ArrayList<>();
        @SerializedName("next_check")
        private long nextCheck;

        public List<Order> pendingOrders() {
            return pendingOrders;
        }

        public long nextCheck() {
            return nextCheck;
        }

        @Override
        public String toString() {
            return "Server{" +
                    "orders=" + pendingOrders +
                    ", nextCheck=" + nextCheck +
                    '}';
        }
    }

    public static class Order {

        private Integer id;
        private String date;
        private String player;
        private String execution;
        private List<Item> items = new ArrayList<>();

        public Integer id() {
            return id;
        }

        public String date() {
            return date;
        }

        public String player() {
            return player;
        }

        public String playerName() {
            if (player.contains(":")) {
                return player.substring(player.indexOf(':') + 1);
            } else {
                return player;
            }
        }

        public UUID playerId() {
            if (player.contains(":")) {
                return UUID.fromString(player.substring(0, player.indexOf(':')));
            } else {
                return PlayerProvider.getUniqueId(player);
            }
        }

        public List<Item> items() {
            return items;
        }

        @NotNull
        public StoreOrder asStoreOrder(@NotNull String provider, @NotNull String group) {
            final StoreOrder order = new StoreOrder(provider, id, group);
            order.setBuyer(playerId());
            if (execution != null) {
                order.setExecution(StoreOrder.Execution.valueOf(execution.toUpperCase()));
            }
            order.setDate(LocalDate.parse(date));

            for (Item item : items()) {
                order.addItem(group, item.asStoreItem());
            }

            return order;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "id=" + id +
                    ", date='" + date + '\'' +
                    ", player='" + player + '\'' +
                    ", items=" + items +
                    '}';
        }
    }

    public static class Item {

        private Object product;
        private String id;
        private Integer amount;
        private Double price;

        public Object product() {
            return product;
        }

        public String id() {
            return id;
        }

        public Integer amount() {
            return amount;
        }

        public Double price() {
            return price;
        }

        @NotNull
        public StoreOrder.Item asStoreItem() {
            return new StoreOrder.Item(id, price.floatValue()).amount(amount);
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id=" + product +
                    ", key='" + id + '\'' +
                    ", amount=" + amount +
                    ", price=" + price +
                    '}';
        }
    }

    public static class Update {

        @SerializedName("processed_orders")
        private List<Integer> processedOrders;

        public Update() {
        }

        public Update(List<Integer> processedOrders) {
            this.processedOrders = processedOrders;
        }

        public List<Integer> processedOrders() {
            return processedOrders;
        }
    }
}