package com.saicone.pixelbuy.module.data.client;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.module.data.DataClient;
import com.saicone.pixelbuy.api.object.StoreUser;

import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase implements DataClient {

    private final PixelBuy pl = PixelBuy.get();

    private boolean enabled = false;

    public Connection con = null;

    public String getType() {
        return "MYSQL";
    }

    public boolean setup() {
        if (enabled) disable(false);
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            con = DriverManager.getConnection("jdbc:mysql://" +
                            pl.configString("Database.Host") + "/" +
                            pl.configString("Database.Database") +
                            pl.configString("Database.Flags"),
                    pl.configString("Database.User"),
                    pl.configString("Database.Password"));

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

    public void saveData(StoreUser data) {
        String player = data.getPlayer().toLowerCase();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        query("INSERT INTO `PlayerOrders` (PLAYER, DATA) VALUES ('" + player + "','" + json + "') " + "ON DUPLICATE KEY UPDATE `DATA` = '" + json + "';", data);
    }

    public StoreUser getData(String player) {
        StoreUser data = null;
        try {
            Statement stmt = con.createStatement();
            ResultSet rS = stmt.executeQuery("SELECT `DATA` FROM `PlayerOrders` WHERE `PLAYER` = '" + player + "';");
            rS.last();
            Gson gson = new Gson();
            data = gson.fromJson(rS.getString("DATA"), StoreUser.class);
            stmt.close();
            rS.close();
        } catch (SQLException | NullPointerException e) {
            PixelBuy.log(2, "The data of player " + player + " does not exist");
        }
        return data;
    }

    public List<StoreUser> getAllData() {
        List<StoreUser> datas = new ArrayList<>();
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT `DATA` FROM `PlayerOrders`");
            ResultSet rS = stmt.executeQuery();
            Gson gson = new Gson();
            while (rS.next()) {
                datas.add(gson.fromJson(rS.getString("DATA"), StoreUser.class));
            }
        } catch (SQLException ignored) { }
        return datas;
    }

    public void deleteData(String player) {
        query("DELETE FROM `PlayerOrders` WHERE `PLAYER` = '" + player + "';", null);
    }

    public void query(String sql, StoreUser data) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(sql);
                stmt.close();
            } catch (SQLException e) {
                if (PixelBuy.get().getLang().getLogLevel() >= 2) {
                    PixelBuy.log(2, "There was an error trying to do the Query: '" + sql + "'");
                    e.printStackTrace();
                }
                if (data != null) pl.getDatabase().addCachedData(data);
                disable(true);
            } catch (NullPointerException e) {
                if (PixelBuy.get().getLang().getLogLevel() >= 1) {
                    e.printStackTrace();
                }
                if (data != null) pl.getDatabase().addCachedData(data);
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
                pl.getDatabase().setDefault();
            }
        }
    }
}
