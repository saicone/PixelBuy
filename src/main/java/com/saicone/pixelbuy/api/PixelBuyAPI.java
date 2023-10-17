package com.saicone.pixelbuy.api;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreAction;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PixelBuyAPI {

    public static boolean registerAction(@NotNull String id, @NotNull StoreAction.Builder<?> builder) {
        return PixelBuy.get().getStore().registerAction(id, builder);
    }

    public static boolean unregisterAction(@NotNull String id) {
        return PixelBuy.get().getStore().unregisterAction(id);
    }

    public static boolean unregisterAction(@NotNull StoreAction.Builder<?> builder) {
        return PixelBuy.get().getStore().unregisterAction(builder);
    }

    @NotNull
    public static List<StoreAction> buildActions(@Nullable Object object) {
        final List<StoreAction> actions = new ArrayList<>();
        if (object == null) {
            return actions;
        }

        if (object instanceof ConfigurationSection) {
            for (String id : ((ConfigurationSection) object).getKeys(false)) {
                final StoreAction action = buildAction(id, ((ConfigurationSection) object).get(id));
                if (action != null) {
                    actions.add(action);
                }
            }
        } else if (object instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                final StoreAction action = buildAction(String.valueOf(entry.getKey()), entry.getValue());
                if (action != null) {
                    actions.add(action);
                }
            }
        } else if (object instanceof List) {
            for (Object o : (List<?>) object) {
                actions.addAll(buildActions(o));
            }
        } else {
            final String[] split = String.valueOf(object).split(":", 2);
            final StoreAction action = buildAction(split[0].trim(), split.length > 1 ? split[1].trim() : null);
            if (action != null) {
                actions.add(action);
            }
        }

        return actions;
    }

    @Nullable
    public static StoreAction buildAction(@NotNull String id, @Nullable Object object) {
        return PixelBuy.get().getStore().buildAction(id, object);
    }
}
