package com.saicone.pixelbuy.module.data.sql;

import org.jetbrains.annotations.NotNull;

public enum SqlType {

    MYSQL(true, "jdbc:mysql://{host}:{port}/{database}{flags}", "com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"),
    MARIADB(true, "jdbc:mariadb://{host}:{port}/{database}{flags}", "org.mariadb.jdbc.Driver"),
    POSTGRESQL(true, "jdbc:mariadb://{host}:{port}/{database}{flags}", "org.postgresql.Driver"),
    H2(false, "jdbc:h2:{path}", "org.h2.Driver"),
    SQLITE(false, "jdbc:sqlite:{path}", "org.sqlite.JDBC");

    public static final SqlType[] VALUES = values();

    private final boolean external;
    private final String format;
    private final String driver;

    SqlType(boolean external, @NotNull String format, @NotNull String... drivers) {
        this.external = external;
        this.format = format;
        String found = null;
        for (String driver : drivers) {
            try {
                Class.forName(driver);
                found = driver;
                break;
            } catch (ClassNotFoundException ignored) { }
        }
        this.driver = found;
    }

    public boolean isExternal() {
        return external;
    }

    @NotNull
    public String getFormat() {
        return format;
    }

    @NotNull
    public String getDriver() {
        return driver;
    }

    @NotNull
    public String getUrl(@NotNull String host, int port, @NotNull String database, @NotNull String... flags) {
        String url = format.replace("{host}", host).replace("{port}", String.valueOf(port)).replace("{database}", database);
        if (flags.length < 1) {
            return url.replace("{flags}", "");
        } else {
            return url.replace("{flags}", "?" + String.join("&", flags));
        }
    }

    @NotNull
    public String getUrl(@NotNull String path) {
        return format.replace("{path}", path);
    }
}
