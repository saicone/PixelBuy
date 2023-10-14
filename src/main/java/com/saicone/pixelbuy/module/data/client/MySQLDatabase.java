package com.saicone.pixelbuy.module.data.client;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.module.data.DataClient;
import com.saicone.pixelbuy.api.store.StoreUser;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase implements DataClient {

    private final PixelBuy plugin = PixelBuy.get();

    private boolean enabled = false;

    public Connection con = null;

    @Override
    public @NotNull String getType() {
        return "MYSQL";
    }

    @Override
    public boolean setup() {
        if (enabled) disable(false);
        try {
            Class.forName("com.mysql.jdbc.Driver");

            con = DriverManager.getConnection("jdbc:mysql://" +
                            PixelBuy.settings().getString("Database.Host", "") + "/" +
                            PixelBuy.settings().getString("Database.Database", "") +
                            PixelBuy.settings().getString("Database.Flags", ""),
                    PixelBuy.settings().getString("Database.User", ""),
                    PixelBuy.settings().getString("Database.Password", ""));

        } catch (ClassNotFoundException e) {
            PixelBuy.log(1, "MySQL driver was not found");
            return false;
        } catch (SQLException e) {
            PixelBuy.log(1, "Unable to connect to database, check configuration");
            return false;
        } catch (Exception e) {
            if (PixelBuy.get().getLang().getLogLevel() >= 1) {
                PixelBuy.log(1, "There was an unknown error:");
                e.printStackTrace();
            }
            return false;
        }
        enabled = true;
        query("CREATE TABLE IF NOT EXISTS `PlayerOrders` (`PLAYER` varchar(255) NOT NULL,`DATA` TEXT, PRIMARY KEY (`PLAYER`));", null);
        return true;
    }

    @Override
    public void saveData(@NotNull StoreUser data) {
        final String player = data.getPlayer().toLowerCase();
        final Gson gson = new Gson();
        final String json = gson.toJson(data);
        query("INSERT INTO `PlayerOrders` (PLAYER, DATA) VALUES ('" + player + "','" + json + "') " + "ON DUPLICATE KEY UPDATE `DATA` = '" + json + "';", data);
    }

    @Override
    public @Nullable StoreUser getData(@NotNull String player) {
        StoreUser data = null;
        try (Statement stmt = con.createStatement()) {
            final ResultSet rS = stmt.executeQuery("SELECT `DATA` FROM `PlayerOrders` WHERE `PLAYER` = '" + player + "';");
            rS.last();
            final Gson gson = new Gson();
            data = gson.fromJson(rS.getString("DATA"), StoreUser.class);
            rS.close();
        } catch (SQLException | NullPointerException e) {
            PixelBuy.log(2, "The data of player " + player + " does not exist");
        }
        return data;
    }

    @Override
    public @NotNull List<StoreUser> getAllData() {
        final List<StoreUser> datas = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement("SELECT `DATA` FROM `PlayerOrders`")) {
            final ResultSet rS = stmt.executeQuery();
            final Gson gson = new Gson();
            while (rS.next()) {
                datas.add(gson.fromJson(rS.getString("DATA"), StoreUser.class));
            }
        } catch (SQLException ignored) { }
        return datas;
    }

    @Override
    public void deleteData(@NotNull String player) {
        query("DELETE FROM `PlayerOrders` WHERE `PLAYER` = '" + player + "';", null);
    }

    public void query(String sql, StoreUser data) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                if (PixelBuy.get().getLang().getLogLevel() >= 2) {
                    PixelBuy.log(2, "There was an error trying to do the Query: '" + sql + "'");
                    e.printStackTrace();
                }
                if (data != null) {
                    plugin.getDatabase().addCachedData(data);
                }
                disable(true);
            } catch (NullPointerException e) {
                if (PixelBuy.get().getLang().getLogLevel() >= 1) {
                    e.printStackTrace();
                }
                if (data != null) {
                    plugin.getDatabase().addCachedData(data);
                }
            }
        });
    }

    public void disable(boolean reconnect) {
        PixelBuy.log(4, "Disconnecting database ...");
        try {
            con.close();
        } catch (SQLException e) {
            if (PixelBuy.get().getLang().getLogLevel() >= 2) {
                PixelBuy.log(2, "There was an error trying to disconnect from the database:");
                e.printStackTrace();
            }
        }
        if (reconnect) {
            PixelBuy.log(3, "Reconnecting with the database...");
            if (!setup()) {
                PixelBuy.log(1, "Could not reconnect to database, JSON will be used as change");
                plugin.getDatabase().setDefault();
            }
        }
    }
}
