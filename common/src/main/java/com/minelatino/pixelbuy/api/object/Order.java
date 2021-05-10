package com.minelatino.pixelbuy.api.object;

import java.util.Map;

public class Order {

    private final Integer id;
    // Item states:
    // 1 = Pending
    // 2 = Processed
    // 3 = Refunded
    private Map<String, Byte> items;

    public Order(Integer id, Map<String, Byte> items) {
        this.id = id;
        this.items = items;
    }

    public Integer getId() {
        return id;
    }

    public Map<String, Byte> getItems() {
        return items;
    }

    public void setItems(Map<String, Byte> items) {
        this.items = items;
    }

    public boolean hasType(byte type) {
        return items.containsValue(type);
    }
}
