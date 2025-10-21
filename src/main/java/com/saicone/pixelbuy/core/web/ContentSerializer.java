package com.saicone.pixelbuy.core.web;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public interface ContentSerializer {

    ContentSerializer GSON = gson(new Gson());

    @NotNull
    static ContentSerializer gson(@NotNull Gson gson) {
        return new ContentSerializer() {
            @Override
            public @NotNull String type() {
                return "application/json";
            }

            @Override
            public <T> @Nullable T read(@NotNull Class<T> type, @NotNull URLConnection con) throws IOException {
                try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(con.getInputStream()), StandardCharsets.UTF_8)) {
                    return gson.fromJson(reader, type);
                }
            }

            @Override
            public void write(@NotNull URLConnection con, @NotNull Object object) throws IOException {
                final byte[] input = gson.toJson(object).getBytes(StandardCharsets.UTF_8);
                try (OutputStream out = con.getOutputStream()) {
                    out.write(input, 0, input.length);
                }
            }
        };
    }

    @NotNull
    String type();

    @Nullable
    <T> T read(@NotNull Class<T> type, @NotNull URLConnection con) throws IOException;

    void write(@NotNull URLConnection con, @NotNull Object object) throws IOException;
}
