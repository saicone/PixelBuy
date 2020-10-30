package com.minelatino.pixelbuy.managers.database.types;

import com.google.gson.Gson;
import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.database.DatabaseType;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import com.minelatino.pixelbuy.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.*;

public class MySQL implements DatabaseType {

    private final PixelBuy pl = PixelBuy.get();

    private boolean enabled = false;

    private boolean debug = false;

    public Connection con = null;

    public String getType() {
        return "MYSQL";
    }

    public boolean setup() {
        if (enabled) disable(false);
        YamlConfiguration SETTINGS = pl.getFiles().getSettings();
        debug = SETTINGS.getBoolean("Database.Debug");
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            con = DriverManager.getConnection("jdbc:mysql://" +
                            SETTINGS.getString("Database.Host") + "/" +
                            SETTINGS.getString("Database.Database") +
                            SETTINGS.getString("Database.Flags"),
                            SETTINGS.getString("Database.User"),
                            SETTINGS.getString("Database.Password"));

        } catch (ClassNotFoundException e) {
            if (debug) Utils.info(pl.getFiles().getMessages().getString("asd"));
            return false;
        } catch (SQLException e) {
            if (debug) Utils.info(pl.getFiles().getMessages().getString(""));
            return false;
        } catch (Exception e) {
            if (debug) Utils.info(pl.getFiles().getMessages().getString("xd"));
            return false;
        }
        enabled = true;
        query("CREATE TABLE IF NOT EXISTS `PlayerOrders` (`PLAYER` varchar(255) NOT NULL,`DATA` TEXT, PRIMARY KEY (`PLAYER`));");
        return true;
    }

    public void saveData(PlayerData data) {
        String player = data.getPlayer();
        PlayerData oldData = getData(player);
        if (oldData != null) data.addCommands(oldData.getCommands());
        Gson gson = new Gson();
        String json = gson.toJson(data);
        query("INSERT INTO `PlayerOrders` (PLAYER, DATA) VALUES ('" + player + "','" + json + "') " + "ON DUPLICATE KEY UPDATE `DATA` = '" + json + "';");
    }

    public PlayerData getData(String player) {
        PlayerData data = null;
        try {
            Statement stmt = con.createStatement();
            ResultSet rS = stmt.executeQuery("SELECT `DATA` FROM `PlayerOrders` WHERE `PLAYER` = '" + player + "';");
            rS.last();
            Gson gson = new Gson();
            data = gson.fromJson(rS.getString("DATA"), PlayerData.class);
            stmt.close();
            rS.close();
        } catch (SQLException | NullPointerException e) {
            if (debug) {
                Utils.info(pl.getFiles().getMessages().getString("xd"));
                Utils.info(e.getMessage());
            }
        }
        return data;
    }

    public void deleteData(String player) {
        query("DELETE FROM `PlayerOrders` WHERE `PLAYER` = '" + player + "';");
    }

    public void query(String sql) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(sql);
                stmt.close();
            } catch (SQLException e) {
                if (debug) {
                    Utils.info(pl.getFiles().getMessages().getString("xd"));
                    Utils.info(e.getMessage());
                }
                disable(true);
            } catch (NullPointerException e) {
                if (debug) {
                    Utils.info(pl.getFiles().getMessages().getString("xd"));
                    Utils.info(e.getMessage());
                }
            }
        });
    }

    public void disable(boolean reconnect) {
        try {
            con.close();
        } catch (SQLException e) {
            if (debug) {
                Utils.info(pl.getFiles().getMessages().getString("xd"));
                Utils.info(e.getMessage());
            }
        }
        if (reconnect) {
            if (!setup()) pl.getDatabase().setDefault();
        }
    }
}
