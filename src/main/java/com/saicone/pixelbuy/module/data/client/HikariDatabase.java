package com.saicone.pixelbuy.module.data.client;

import com.google.common.reflect.TypeToken;
import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreOrder;
import com.saicone.pixelbuy.module.data.DataClient;
import com.saicone.pixelbuy.api.store.StoreUser;

import com.saicone.pixelbuy.module.data.sql.SqlSchema;
import com.saicone.pixelbuy.module.data.sql.SqlType;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.util.OptionalType;
import com.saicone.pixelbuy.util.Strings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

public class HikariDatabase implements DataClient {

    private static final SqlSchema SCHEMA = new SqlSchema(SqlType.MYSQL);
    private static final String USERS_TABLE = "{prefix}users";
    private static final String ORDERS_TABLE = "{prefix}orders";
    private static final Type ITEM_TYPE = new TypeToken<Map<String, Set<StoreOrder.Item>>>() {}.getType();

    private SqlType type;
    private String prefix;
    private HikariConfig hikariConfig;
    private HikariDataSource hikari;

    @Override
    public void onLoad(@NotNull BukkitSettings config) {
        this.type = null;
        final String type = config.getIgnoreCase("type").asString("mysql");
        for (SqlType value : SqlType.VALUES) {
            if (value.name().equalsIgnoreCase(type)) {
                this.type = value;
            }
        }

        this.prefix = config.getRegex("(?i)(tables?-?)?prefix").asString("pixebuy_");

        if (this.type == null) {
            PixelBuy.log(1, "Cannot initialize SQL database, the sql type '" + type + "' doesn't exists");
            this.hikariConfig = null;
            return;
        }

        if (!SCHEMA.isLoaded()) {
            try {
                SCHEMA.load("com/saicone/pixelbuy/module/data/schema");
            } catch (IOException e) {
                PixelBuy.logException(1, e, "Cannot load SQL schema");
            }
        }

        this.hikariConfig = new HikariConfig();

        if (this.type.isExternal()) {
            final String host = config.getIgnoreCase("host").asString("localhost");
            final int port = config.getIgnoreCase("port").asInt(3306);
            final String database = config.getRegex("(?i)(db|database)(-?name)?").asString("database");
            final String[] flags = config.getRegex("(?i)flags?|propert(y|ies)").asList(OptionalType::asString).toArray(new String[0]);

            this.hikariConfig.setJdbcUrl(this.type.getUrl(host, port, database, flags));
            this.hikariConfig.setUsername(config.getRegex("(?i)user(-?name)?").asString("root"));
            this.hikariConfig.setPassword(config.getIgnoreCase("password").asString("password"));
        } else {
            final String path = config.getIgnoreCase("path").asString("plugins/PixelBuy/database/" + this.type.name().toLowerCase() + ".db");

            this.hikariConfig.setJdbcUrl(this.type.getUrl(path));
        }
    }

    @Override
    public void onStart() {
        if (hikariConfig == null) {
            return;
        }
        hikari = new HikariDataSource(hikariConfig);
        connectSync(con -> {
            onStartTable(con, parse(USERS_TABLE), "create:users_table");
            onStartTable(con, parse(ORDERS_TABLE), "create:orders_table");
        });
    }

    private void onStartTable(@NotNull Connection con, @NotNull String tableName, @NotNull String queryKey) throws SQLException {
        // This table creation process was taken from LuckPerms
        if (isTablePresent(con, tableName)) {
            return;
        }

        final List<String> list = SCHEMA.getList(type, queryKey);
        boolean next = true;

        try (Statement stmt = con.createStatement()) {
            for (String sql : list) {
                stmt.addBatch(parse(sql, "utf8mb4"));
            }

            try {
                stmt.executeBatch();
            } catch (BatchUpdateException e) {
                if (e.getMessage().contains("Unknown character set")) {
                    next = false;
                } else {
                    throw e;
                }
            }
        }

        if (next) {
            return;
        }

        try (Statement stmt = con.createStatement()) {
            for (String sql : list) {
                stmt.addBatch(parse(sql, "utf8"));
            }

            stmt.executeBatch();
        }
    }

    @Override
    public void onClose() {
        hikari.close();
    }

    @Override
    public void getUser(@NotNull UUID uniqueId, @NotNull String username, @NotNull Consumer<StoreUser> consumer) {
        connect(con -> {
            UUID foundId;
            try (PreparedStatement stmt = con.prepareStatement(schema("select:user"))) {
                stmt.setString(1, username);
                final ResultSet result = stmt.executeQuery();
                while (result.next()) {
                    foundId = UUID.fromString(result.getString("uuid"));
                    if (foundId.equals(uniqueId)) {
                        final StoreUser user = new StoreUser(uniqueId, username, result.getFloat("donated"));
                        if (!username.equalsIgnoreCase(result.getString("username"))) {
                            saveUser(user);
                        }
                        consumer.accept(user);
                        return;
                    } else {
                        PixelBuy.log(2, "Found duplicated UUID " + foundId + " for username '" + username + "' with id " + uniqueId);
                        saveUser(new StoreUser(uniqueId, null, 0.0f));
                    }
                }
            }
            consumer.accept(new StoreUser(uniqueId, username, 0.0f));
        });
    }

    @Override
    public void getUsers(@NotNull Consumer<StoreUser> consumer) {
        connect(con -> {
            try (PreparedStatement stmt = con.prepareStatement(schema("select:users"))) {
                final ResultSet result = stmt.executeQuery();
                while (result.next()) {
                    final String username = result.getString("username");
                    if (username != null) {
                        consumer.accept(new StoreUser(UUID.fromString(result.getString("uuid")), username, result.getFloat("donated")));
                    }
                }
            }
        });
    }

    @Override
    public void getOrders(@NotNull UUID buyer, @NotNull Consumer<StoreOrder> consumer) {
        connect(con -> {
            try (PreparedStatement stmt = con.prepareStatement(schema("select:orders"))) {
                stmt.setString(1, buyer.toString());
                final ResultSet result = stmt.executeQuery();
                while (result.next()) {
                    final StoreOrder order = new StoreOrder(result.getString("provider"), result.getInt("order"), result.getString("group"));
                    order.setDataId(result.getInt("id"));
                    order.setBuyer(buyer);
                    final String time = result.getString("time");
                    if (time != null) {
                        int ordinal = 0;
                        for (String s : time.split(",")) {
                            order.setDate(ordinal, LocalDate.parse(s));
                            ordinal++;
                        }
                    }
                    final String execution = result.getString("execution");
                    if (execution != null) {
                        order.setExecution(StoreOrder.Execution.valueOf(execution));
                    }
                    final String items = result.getString("items");
                    if (items != null) {
                        final Map<String, Set<StoreOrder.Item>> map = OptionalType.GSON.fromJson(items, ITEM_TYPE);
                        for (var entry : map.entrySet()) {
                            for (StoreOrder.Item item : entry.getValue()) {
                                order.addItem(entry.getKey(), item);
                            }
                        }
                    }
                    order.setEdited(false);
                    consumer.accept(order);
                }
            }
        });
    }

    @Override
    public void saveUser(@NotNull StoreUser user) {
        if (!user.isEdited()) {
            return;
        }
        connect(con -> {
            saveOrders(con, user.getOrders());

            try (PreparedStatement stmt = con.prepareStatement(schema("insert:user"))) {
                stmt.setString(1, user.getUniqueId().toString());
                stmt.setString(2, user.getName());
                stmt.setFloat(3, user.getDonated());
                stmt.execute();
            }
        });
    }

    @Override
    public void saveUsers(@NotNull Collection<StoreUser> users) {
        if (users.isEmpty()) {
            return;
        }
        connect(con -> {
            try (PreparedStatement stmt = con.prepareStatement(schema("insert:user"))) {
                for (StoreUser user : users) {
                    if (!user.isEdited()) {
                        continue;
                    }

                    saveOrders(con, user.getOrders());

                    stmt.setString(1, user.getUniqueId().toString());
                    stmt.setString(2, user.getName());
                    stmt.setFloat(3, user.getDonated());

                    stmt.addBatch();
                }

                stmt.executeBatch();
            }
        });
    }

    @Override
    public void saveOrders(@NotNull Collection<StoreOrder> orders) {
        if (orders.isEmpty()) {
            return;
        }
        connect(con -> saveOrders(con, orders));
    }

    private void saveOrders(@NotNull Connection con, @NotNull Collection<StoreOrder> orders) throws SQLException {
        if (orders.isEmpty()) {
            return;
        }
        try (PreparedStatement insert = con.prepareStatement(schema("insert:order")); PreparedStatement update = con.prepareStatement(schema("update:order"))) {
            for (StoreOrder order : orders) {
                if (order.getBuyer() == null || !order.isEdited()) {
                    continue;
                }
                if (order.getDataId() < 1) {
                    insert.setString(1, order.getProvider());
                    insert.setInt(2, order.getId());
                    insert.setString(3, order.getGroup());
                    setOrder(insert, order, 4);
                    insert.addBatch();
                } else {
                    setOrder(update, order, 1);
                    update.setInt(5, order.getDataId());
                    update.addBatch();
                }
            }

            insert.executeBatch();
            update.executeBatch();
        }
    }

    private void setOrder(@NotNull PreparedStatement stmt, @NotNull StoreOrder order, int start) throws SQLException {
        stmt.setString(start, order.getBuyer().toString());
        final StringJoiner joiner = new StringJoiner(",");
        for (LocalDate date : order.getDates()) {
            joiner.add(date.toString());
        }
        stmt.setString(start + 1, joiner.toString());
        stmt.setString(start + 2, order.getExecution().name());
        stmt.setString(start + 3, OptionalType.GSON.toJson(order.getItems()));
    }

    @NotNull
    private String schema(@NotNull String key) {
        return parse(SCHEMA.get(type, key));
    }

    @NotNull
    private String parse(@NotNull String s, @NotNull Object... args) {
        return Strings.replaceArgs(s.replace("{prefix}", prefix), args);
    }

    private static boolean isTablePresent(@NotNull Connection con, @NotNull String tableName) throws SQLException {
        try (ResultSet set = con.getMetaData().getTables(con.getCatalog(), null, "%", null)) {
            while (set.next()) {
                if (set.getString(3).equals(tableName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void connect(@NotNull SqlConsumer consumer) {
        if (hikari == null || hikari.isClosed()) {
            return;
        }

        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(PixelBuy.get(), () -> connectSync(consumer));
        } else {
            connectSync(consumer);
        }
    }

    public void connectSync(@NotNull SqlConsumer consumer) {
        try (Connection connection = hikari.getConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            PixelBuy.logException(2, e);
        }
    }

    @FunctionalInterface
    public interface SqlConsumer {
        void accept(@NotNull Connection connection) throws SQLException;
    }
}
