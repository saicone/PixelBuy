package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;

public class StoreCategory {

    private final String id;

    private float discount = 0.0f;

    public StoreCategory(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public float getDiscount() {
        return discount;
    }

    public void onReload(@NotNull BukkitSettings config) {
        String discount = config.getIgnoreCase("discount").asString();
        if (discount == null || discount.isBlank()) {
            this.discount = 0.0f;
        } else {
            if (discount.charAt(0) == '.') {
                discount = "0" + discount;
            }
            final int last = discount.length() - 1;
            try {
                if (discount.charAt(last) == '%') {
                    this.discount = Float.parseFloat(discount.substring(0, last)) / 100f;
                } else {
                    this.discount = Float.parseFloat(discount);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}
