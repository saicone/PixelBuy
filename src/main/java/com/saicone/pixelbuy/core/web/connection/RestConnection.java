package com.saicone.pixelbuy.core.web.connection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.saicone.pixelbuy.core.web.ContentSerializer;
import com.saicone.pixelbuy.core.web.WebConnection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public abstract class RestConnection<R extends RestConnection<R>> implements WebConnection {

    private static final Object DUMMY = new Object();

    @NotNull
    public static Params params(@NotNull String url, @NotNull String... params) {
        return params(ContentSerializer.GSON, url, params);
    }

    @NotNull
    public static Params params(@NotNull ContentSerializer content, @NotNull String url, @NotNull String... params) {
        return new Params(content, url, params);
    }

    @NotNull
    public static Header header(@NotNull String url, @NotNull String... properties) {
        return header(ContentSerializer.GSON, url, properties);
    }

    @NotNull
    public static Header header(@NotNull ContentSerializer content, @NotNull String url, @NotNull String... properties) {
        return new Header(content, url, properties);
    }

    @NotNull
    public static Basic basic(@NotNull String url, @NotNull String credentials) {
        return basic(ContentSerializer.GSON, url, credentials);
    }

    @NotNull
    public static Basic basic(@NotNull ContentSerializer content, @NotNull String url, @NotNull String credentials) {
        return new Basic(content, url, credentials);
    }

    private final ContentSerializer content;
    private final String url;

    private Cache<String, Object> cache;

    public RestConnection(@NotNull ContentSerializer content, @NotNull String url) {
        this.content = content;
        this.url = url;
    }

    @NotNull
    protected abstract R get();

    @NotNull
    public ContentSerializer content() {
        return content;
    }

    @NotNull
    public String url() {
        return url;
    }

    @NotNull
    public String url(@NotNull Object... args) {
        return replace(url, args);
    }

    @NotNull
    @Contract("_, _ -> this")
    public R cache(long duration, @NotNull TimeUnit unit) {
        if (duration > 0) {
            this.cache = CacheBuilder.newBuilder().expireAfterWrite(duration, unit).build();
        }
        return get();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> T caching(@NotNull String key, @NotNull IOSupplier<T> supplier) throws IOException {
        if (this.cache == null) {
            return supplier.get();
        }

        Object cached = this.cache.getIfPresent(key);
        if (cached == null) {
            cached = supplier.get();
            if (cached == null) {
                cached = DUMMY;
            }
            this.cache.put(key, cached);
        }
        if (cached == DUMMY) {
            return null;
        }
        return (T) cached;
    }

    @NotNull
    protected HttpsURLConnection connection(@NotNull String url) throws IOException {
        return (HttpsURLConnection) new URL(url).openConnection();
    }

    @Override
    public <T> @Nullable T fetch(@NotNull Class<T> contentType, @NotNull Object... args) throws IOException {
        final String url = url(args);
        return caching(url, () -> {
            final HttpsURLConnection con = connection(url);
            try {
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Accept", content().type());

                return content().read(contentType, con);
            } finally {
                con.disconnect();
            }
        });
    }

    @Override
    public <T> @Nullable T send(@NotNull Class<T> responseType, @NotNull Object object, @NotNull Object... args) throws IOException {
        final String url = url(args);
        final HttpsURLConnection con = connection(url);

        try {
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", content().type() + "; charset=utf-8");
            con.setRequestProperty("Accept", content().type());
            con.setDoOutput(true);

            content().write(con, object);

            return content().read(responseType, con);
        } finally {
            con.disconnect();
        }
    }

    @NotNull
    protected String replace(@NotNull String s, @NotNull Object... args) {
        for (int i = 0; i + 1 < args.length; i += 2) {
            s = s.replace(String.valueOf(args[i]), String.valueOf(args[i + 1]));
        }
        return s;
    }

    @FunctionalInterface
    protected interface IOSupplier<T> {
        T get() throws IOException;
    }

    public enum Type {
        PARAMS,
        HEADER,
        BASIC;

        @NotNull
        public static Optional<Type> of(@Nullable String s) {
            if (s == null) {
                return Optional.empty();
            }
            for (Type value : values()) {
                if (value.name().equalsIgnoreCase(s)) {
                    return Optional.of(value);
                }
            }
            return Optional.empty();
        }
    }

    public static class Params extends RestConnection<Params> {

        public Params(@NotNull ContentSerializer content, @NotNull String url, @NotNull String... params) {
            super(content, url + (url.contains("?") ? "&" : "?") + formatParams(params));
        }

        @Override
        protected @NotNull Params get() {
            return this;
        }

        @NotNull
        private static String formatParams(@NotNull String... params) {
            final StringJoiner joiner = new StringJoiner("&");
            for (int i = 0; i + 1 < params.length; i += 2) {
                joiner.add(params[i] + "=" + params[i + 1]);
            }
            return joiner.toString();
        }
    }

    public static class Header extends RestConnection<Header> {

        private final String[] properties;

        public Header(@NotNull ContentSerializer content, @NotNull String url, @NotNull String... properties) {
            super(content, url);
            this.properties = properties;
        }

        @Override
        protected @NotNull Header get() {
            return this;
        }

        @Override
        protected @NotNull HttpsURLConnection connection(@NotNull String url) throws IOException {
            final HttpsURLConnection con = super.connection(url);
            for (int i = 0; i + 1 < properties.length; i += 2) {
                con.setRequestProperty(properties[i], properties[i + 1]);
            }
            return con;
        }
    }

    public static class Basic extends RestConnection<Basic> {

        private final String authorization;

        public Basic(@NotNull ContentSerializer content, @NotNull String url, @NotNull String credentials) {
            super(content, url);
            final String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
            this.authorization = "Basic " + encoded;
        }

        @Override
        protected @NotNull Basic get() {
            return this;
        }

        @Override
        protected @NotNull HttpsURLConnection connection(@NotNull String url) throws IOException {
            final HttpsURLConnection con = super.connection(url);
            con.setRequestProperty("Authorization", authorization);
            return con;
        }
    }
}
