package com.minelatino.pixelbuy.managers.player;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {

    private String player;
    private List<Order> orders;

    public PlayerData(String player, List<Order> orders) {
        this.player = player;
        this.orders = orders;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<String> getCommands() {
        List<String> cmds = new ArrayList<>();
        orders.forEach(order -> cmds.addAll(order.getCmds()));
        return cmds;
    }

    public void addOrders(List<Order> orders) {
        this.orders.addAll(orders);
    }
}
