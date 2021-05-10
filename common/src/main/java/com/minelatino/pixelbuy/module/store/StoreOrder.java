package com.minelatino.pixelbuy.module.store;

import java.util.List;

public class StoreOrder {
    private String player;
    private final Integer order_id;
    private final List<String> commands;

    public StoreOrder(String player, Integer order_id, List<String> commands) {
        this.player = player;
        this.order_id = order_id;
        this.commands = commands;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Integer getOrderId() {
        return order_id;
    }

    public List<String> getItems() {
        return commands;
    }
}
