package com.minelatino.pixelbuy.managers.player;

import java.util.List;

public class PlayerData {

    private String player;
    private Integer orderId;
    private List<String> commands;

    public PlayerData(String player, Integer orderId, List<String> commands) {
        this.player = player;
        this.orderId = orderId;
        this.commands = commands;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void addCommands(List<String> commands) {
        this.commands.addAll(commands);
    }
}
