package com.saicone.pixelbuy.module.data.sql;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum SqlType {

    MYSQL(
            true,
            "jdbc:mysql://{host}:{port}/{database}{flags}",
            "com{}mysql:mysql-connector-j:8.2.0",
            Map.of("com{}mysql{}cj{}jdbc{}Driver", "com.mysql.cj.jdbc.Driver", "com{}mysql{}jdbc{}Driver", "com.mysql.jdbc.Driver")
    ),
    MARIADB(
            true,
            "jdbc:mariadb://{host}:{port}/{database}{flags}",
            "org{}mariadb{}jdbc:mariadb-java-client:3.2.0",
            Map.of("org{}mariadb{}jdbc{}Driver", "org.mariadb.jdbc.Driver")
    ),
    POSTGRESQL(
            true,
            "jdbc:mariadb://{host}:{port}/{database}{flags}",
            "org{}postgresql:postgresql:42.6.0",
            Map.of("org{}postgresql{}Driver", "org.postgresql.Driver")
    ),
    H2(
            false,
            "jdbc:h2:./{path}",
            "com{}h2database:h2:2.2.224",
            Map.of("org{}h2{}Driver", "org.h2.Driver")
    ),
    SQLITE(
            false,
            "jdbc:sqlite:{path}.db",
            "org{}xerial:sqlite-jdbc:3.43.2.2",
            Map.of("org{}sqlite{}JDBC", "org.sqlite.JDBC")
    );

    public static final SqlType[] VALUES = values();

    private final boolean external;
    private final String format;
    private final String dependency;
    private final Map<String, String> relocation;

    SqlType(boolean external, @NotNull String format, @NotNull String dependency, @NotNull Map<String, String> relocation) {
        this.external = external;
        this.format = format;
        this.dependency = dependency.replace("{}", ".");
        this.relocation = new HashMap<>();
        relocation.forEach((key, value) -> this.relocation.put(key.replace("{}", "."), value));
    }

    public boolean isExternal() {
        return external;
    }

    public boolean isDriverPresent() {
        for (Map.Entry<String, String> entry : relocation.entrySet()) {
            try {
                Class.forName(entry.getValue());
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
    public Map<String, String> getRelocation() {
        return relocation;
    }

    @NotNull
    public Set<String> getDrivers() {
        return relocation.keySet();
    }

    @NotNull
    public String getDriver() {
        for (Map.Entry<String, String> entry : relocation.entrySet()) {
            try {
                Class.forName(entry.getValue());
                return entry.getValue();
            } catch (ClassNotFoundException ignored) { }
        }
        throw new RuntimeException("Cannot find driver class name for sql type: " + name());
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

    @Nullable
    @Contract("_, !null -> !null")
    public static SqlType of(@NotNull String name, @Nullable SqlType def) {
        for (SqlType value : VALUES) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return def;
    }
}
