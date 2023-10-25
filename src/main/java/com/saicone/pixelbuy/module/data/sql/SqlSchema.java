package com.saicone.pixelbuy.module.data.sql;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class SqlSchema {

    private final SqlType defaultType;

    private boolean loaded;
    private final Map<SqlType, Map<String, List<String>>> queries = new HashMap<>();

    public SqlSchema(@NotNull SqlType defaultType) {
        this.defaultType = defaultType;
    }

    @NotNull
    public SqlType getDefaultType() {
        return defaultType;
    }

    @NotNull
    public String get(@NotNull SqlType sql, @NotNull String type) {
        final Map<String, List<String>> map = queries.containsKey(sql) ? queries.get(sql) : queries.get(defaultType);
        return map.get(type).get(0);
    }

    @NotNull
    public List<String> getList(@NotNull SqlType sql, @NotNull String type) {
        final Map<String, List<String>> map = queries.containsKey(sql) ? queries.get(sql) : queries.get(defaultType);
        return map.get(type);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void load(@NotNull String path) throws IOException {
        load(path, SqlSchema.class.getClassLoader());
    }

    public void load(@NotNull ClassLoader classLoader) throws IOException {
        load(SqlSchema.class.getPackageName().replace('.', '/'), classLoader);
    }

    public void load(@NotNull String path, @NotNull ClassLoader classLoader) throws IOException {
        for (SqlType type : SqlType.VALUES) {
            final InputStream in = classLoader.getResourceAsStream(path + '/' + type.name().toLowerCase() + ".sql");
            if (in != null) {
                load(type, new BufferedInputStream(in));
            }
        }
    }

    public void load(@NotNull SqlType type, @NotNull InputStream in) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(in)) {
            load(type, reader);
        }
    }

    public void load(@NotNull SqlType type, @NotNull Reader reader) throws IOException {
        String queryType = null;
        List<String> queries = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        try (BufferedReader bf = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
            String line;
            while ((line = bf.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.startsWith("--")) {
                    if (line.length() == 1 || line.substring(1).isBlank()) {
                        continue;
                    }
                    if (queryType != null) {
                        this.queries.computeIfAbsent(type, __ -> new HashMap<>()).put(queryType, queries);
                        queries = new ArrayList<>();
                        builder = new StringBuilder();
                    }
                    queryType = line.substring(1).trim();
                    continue;
                }

                if (line.endsWith(";")) {
                    builder.append(line, 0, line.length() - 1);
                    String query = builder.toString();
                    if (!query.isBlank()) {
                        queries.add(query);
                    }
                    builder = new StringBuilder();
                } else {
                    builder.append(line);
                }
            }
        }
    }
}
