package com.minelatino.pixelbuy.module.data.type.sql;

import com.minelatino.pixelbuy.util.PixelUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SqlHandler {

    private static final Map<String, SqlConnection> connections = new HashMap<>();

    public static SqlConnection getSqlConnection(String id) {
        return connections.get(id);
    }

    public static Connection getConnection(String id) {
        if (connections.containsKey(id)) {
            return getSqlConnection(id).getConnection();
        } else {
            return null;
        }
    }

    public static String createConnection(String sqlClass, String url, String user, String password) {
        for (Map.Entry<String, SqlConnection> entry : connections.entrySet()) {
            if (entry.getValue().isEqual(sqlClass, url, user, password)) {
                return entry.getKey();
            }
        }
        String id = PixelUtils.randomString(4);
        while (connections.containsKey(id)) {
            id = PixelUtils.randomString(4);
        }
        connections.put(id, new SqlConnection(sqlClass, url, user, password));
        return id;
    }

    public static void removeConnection(String id) {

    }

    public static class SqlConnection {

        private Connection con;
        private boolean on = false;
        private int uses = 0;

        private final String sqlClass;
        private final String url;
        private final String user;
        private final String password;

        public SqlConnection(String sqlClass, String url, String user, String password) {
            this.sqlClass = sqlClass;
            this.url = url;
            this.user = user;
            this.password = password;
        }

        public boolean isEqual(String sqlClass, String url, String user, String password) {
            return this.sqlClass.equals(sqlClass) && this.url.equals(url) && this.user.equals(user) && this.password.equals(password);
        }

        public int getUses() {
            return uses;
        }

        public boolean isOn() {
            uses++;
            return on;
        }

        public void setOn(boolean on) {
            if (!on) uses--;
            this.on = on;
        }

        public void setup() throws ClassNotFoundException, SQLException {
            Class.forName(sqlClass);
            con = DriverManager.getConnection(url, user, password);
        }

        public void disconnect() throws SQLException {
            con.close();
        }

        public Connection getConnection() {
            return con;
        }

    }
}
