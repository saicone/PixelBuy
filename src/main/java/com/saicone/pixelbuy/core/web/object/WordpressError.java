package com.saicone.pixelbuy.core.web.object;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.UnaryOperator;

public class WordpressError implements WordpressResponse {

    private String code;
    private String message;
    private Data data;

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public Data data() {
        return data;
    }

    @Override
    public boolean isError() {
        return code != null;
    }

    @Override
    public void throwError(@NotNull UnaryOperator<String> filter) throws IOException {
        String filtered = message;
        if (filtered != null) {
            filtered = filter.apply(filtered);
        }
        throw new IOException("[" + code + (data != null ? "/" + data.status() : "") + "]: " + filtered);
    }

    public static class Data {

        private Integer status;

        public Integer status() {
            return status;
        }
    }
}
