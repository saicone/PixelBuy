package com.minelatino.pixelbuy.managers.order.objects;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class WebOrder {

    @SerializedName("player")
    @Expose
    private String player;
    @SerializedName("order_id")
    @Expose
    private Integer orderId;
    @SerializedName("commands")
    @Expose
    private List<String> commands = null;

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public WebOrder withPlayer(String player) {
        this.player = player;
        return this;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public WebOrder withOrderId(Integer orderId) {
        this.orderId = orderId;
        return this;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public WebOrder withCommands(List<String> commands) {
        this.commands = commands;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("player", player).append("orderId", orderId).append("commands", commands).toString();
    }

}