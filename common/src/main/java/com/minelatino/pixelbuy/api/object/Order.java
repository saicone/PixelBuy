package com.minelatino.pixelbuy.api.object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Order {

    private final Integer id;
    private String params = "";
    private boolean refunded = false;
    // Item state:
    // false = Pending
    // true = Processed
    private final Map<String, Boolean> items;
    private final long time;

    public Order(Integer id, List<String> items) {
        this.id = id;
        this.items = new HashMap<>();
        items.forEach(item -> this.items.put(item, false));
        time = System.currentTimeMillis() / 1000;
    }

    public Integer getId() {
        return id;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public void setRefunded(boolean refunded) {
        this.refunded = refunded;
    }

    public String getParams() {
        return params;
    }

    public void addParams(String... s) {
        addParam(String.join(";", s));
    }

    public void addParam(String s) {
        params = params + (params.isEmpty() ? "" : ";") + s;
    }

    public Map<String, Boolean> getItems() {
        return items;
    }

    public List<String> getItems(boolean processed) {
        return items.entrySet().stream().filter(i -> i.getValue().equals(processed)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public void setItemState(String item, boolean state) {
        items.put(item, state);
    }

    public boolean isProcessed() {
        return !items.containsValue(false);
    }

    public long getTime() {
        return time;
    }
}
