package com.minelatino.pixelbuy.api.object;

import java.util.List;

public class WebString {

    private final String code;

    private final String message;

    private Data data;

    private final List<Order> orders;

    public WebString(String code, String message, Data data, List<Order> orders) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.orders = orders;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public static class Data {

        private final Integer status;

        public Data(Integer status) {
            this.status = status;
        }

        public Integer getStatus() {
            return status;
        }
    }

    public static class Order {

        private String player;

        private final Integer order_id;

        private final List<String> commands;

        public Order(String player, Integer order_id, List<String> commands) {
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
}