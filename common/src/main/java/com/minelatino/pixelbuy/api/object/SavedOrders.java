package com.minelatino.pixelbuy.api.object;

import java.util.List;

public class SavedOrders {
    private final List<Integer> processedOrders;

    public SavedOrders(List<Integer> processedOrders) {
        this.processedOrders = processedOrders;
    }

    public List<Integer> getOrders() {
        return processedOrders;
    }
}
