package com.saicone.pixelbuy.module.data.sql;

import org.jetbrains.annotations.NotNull;

public enum SqlType {

    MYSQL(true, "jdbc:mysql://{host}:{port}/{database}{flags}", "mysql:mysql-connector-java:8.0.33", "com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"),
    MARIADB(true, "jdbc:mariadb://{host}:{port}/{database}{flags}", "org.mariadb.jdbc:mariadb-java-client:3.2.0", "org.mariadb.jdbc.Driver"),
    POSTGRESQL(true, "jdbc:mariadb://{host}:{port}/{database}{flags}", "org.postgresql:postgresql:42.6.0", "org.postgresql.Driver"),
    H2(false, "jdbc:h2:{path}", "com.h2database:h2:2.2.224", "org.h2.Driver"),
    SQLITE(false, "jdbc:sqlite:{path}", "org.xerial:sqlite-jdbc:3.43.2.2", "org.sqlite.JDBC");

    public static final SqlType[] VALUES = values();

    private final boolean external;
    private final String format;
    private final String dependency;
    private final String[] drivers;

    SqlType(boolean external, @NotNull String format, @NotNull String dependency, @NotNull String... drivers) {
        this.external = external;
        this.format = format;
        this.dependency = dependency;
        this.drivers = drivers;
    }

    public boolean isExternal() {
        return external;
    }

    public boolean isDriverPresent() {
        for (String driver : drivers) {
            try {
                Class.forName(driver);
                return true;
            } catch (ClassNotFoundException ignored) { }
        }
        return false;
    }

    @NotNull
    public String getFormat() {
        return format;
    }

    @NotNull
    public String getDependency() {
        return dependency;
    }

    @NotNull
    public String[] getDrivers() {
        return drivers;
    }

    @NotNull
    public String getDriver() {
        for (String driver : drivers) {
            try {
                Class.forName(driver);
                return driver;
            } catch (ClassNotFoundException ignored) { }
        }
        throw new RuntimeException("Cannot find driver class name for swl type: " + name());
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
