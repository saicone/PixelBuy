package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.util.OptionalType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class WebValue<T, E> {

    private final T defaultValue;
    private final Map<String, E> providers;
    private final BiFunction<WebSupervisor, E, T> function;

    private Map<String, T> cachedValues;

    @NotNull
    public static <T, E> WebValue<T, E> of(T defaultValue) {
        return new WebValue<>(defaultValue, null, null);
    }

    @NotNull
    public static <T, E> WebValue<T, E> of(@Nullable Object object, Function<OptionalType, T> tFunction, @NotNull Function<OptionalType, E> eFunction, BiFunction<WebSupervisor, E, T> function) {
        if (object instanceof ConfigurationSection) {
            final ConfigurationSection section = (ConfigurationSection) object;
            final T defaultValue = tFunction.apply(OptionalType.of(((ConfigurationSection) object).get("default")));
            final Map<String, E> providers = new HashMap<>();
            for (String key : section.getKeys(false)) {
                if (key.equals("default")) {
                    continue;
                }
                final E e = eFunction.apply(OptionalType.of(section.get(key)));
                if (e != null) {
                    providers.put(key, e);
                }
            }
            if (providers.isEmpty()) {
                return of(defaultValue);
            }
            return new WebValue<>(defaultValue, providers, function);
        } else if (object instanceof Map) {
            return of(BukkitSettings.of(object), tFunction, eFunction, function);
        } else {
            return of(tFunction.apply(OptionalType.of(object)));
        }
    }

    public WebValue(T defaultValue, @Nullable Map<String, E> providers, @Nullable BiFunction<WebSupervisor, E, T> function) {
        this.defaultValue = defaultValue;
        this.providers = providers;
        this.function = function;
    }

    public T get(@Nullable String provider) {
        if (provider != null && providers != null && function != null) {
            if (cachedValues != null) {
                final T t = cachedValues.get(provider);
                return t == null ? defaultValue : t;
            }
            final E e = providers.get(provider);
            if (e != null) {
                final WebSupervisor web = PixelBuy.get().getStore().getSupervisor(provider);
                if (web != null) {
                    try {
                        final T t = function.apply(web, e);
                        if (t != null) {
                            if (cachedValues == null) {
                                cachedValues = new HashMap<>();
                            }
                            cachedValues.put(provider, t);
                            return t;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        if (cachedValues == null) {
                            cachedValues = new HashMap<>();
                        }
                        cachedValues.put(provider, null);
                    }
                }
            }
        }
        return defaultValue;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    public Map<String, E> getProviders() {
        return providers;
    }

    @Nullable
    public E getElement(@NotNull String provider) {
        return providers != null ? providers.get(provider) : null;
    }

    @Nullable
    public BiFunction<WebSupervisor, E, T> getFunction() {
        return function;
    }
}
