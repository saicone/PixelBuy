package com.saicone.pixelbuy.api.store;

import com.google.common.base.Suppliers;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class StoreAction {

    private final Supplier<String> string = Suppliers.memoize(this::toString);

    public void run(@NotNull StoreClient client) {
        // empty default method
    }

    public void run(@NotNull StoreClient client, int amount) {
        run(client);
    }

    @NotNull
    public String asString() {
        return string.get();
    }

    public static class Builder<A extends StoreAction> {

        private final @Language("RegExp") String regex;
        private final Pattern pattern;

        private String defaultKey = "value";
        private BiFunction<String, BukkitSettings, A> accept;

        public Builder(@NotNull @Language("RegExp") String regex) {
            this.regex = regex;
            this.pattern = Pattern.compile(regex);
        }

        @NotNull
        public @Language("RegExp") String getRegex() {
            return regex;
        }

        @NotNull
        public Pattern getPattern() {
            return pattern;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<A> defaultKey(@NotNull String defaultKey) {
            this.defaultKey = defaultKey;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<A> accept(@NotNull Function<BukkitSettings, A> accept) {
            return accept((id, config) -> accept.apply(config));
        }

        @NotNull
        @Contract("_ -> this")
        public Builder<A> accept(@NotNull BiFunction<String, BukkitSettings, A> accept) {
            this.accept = accept;
            return this;
        }

        @Nullable
        protected BukkitSettings parseSettings(@NotNull Object object) {
            return BukkitSettings.of(object);
        }

        @NotNull
        public A build(@NotNull String id, @Nullable Object object) throws IllegalArgumentException {
            final BukkitSettings config;
            if (object instanceof BukkitSettings) {
                config = (BukkitSettings) object;
            } else if (object instanceof ConfigurationSection || object instanceof Map) {
                config = parseSettings(object);
            } else {
                config = new BukkitSettings();
                config.set(defaultKey, object);
            }
            try {
                return accept.apply(id, config);
            } catch (Throwable t) {
                throw new IllegalArgumentException("Cannot convert the provided object to required action type", t);
            }
        }
    }
}
