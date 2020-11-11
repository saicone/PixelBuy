package com.minelatino.pixelbuy.managers.order.objects;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class WebString {

    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("orders")
    @Expose
    private List<WebOrder> webOrders = null;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public WebString withCode(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public WebString withMessage(String message) {
        this.message = message;
        return this;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public WebString withData(Data data) {
        this.data = data;
        return this;
    }

    public List<WebOrder> getOrders() {
        return webOrders;
    }

    public void setOrders(List<WebOrder> webOrders) {
        this.webOrders = webOrders;
    }

    public WebString withOrders(List<WebOrder> webOrders) {
        this.webOrders = webOrders;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("code", code).append("message", message).append("data", data).append("orders", webOrders).toString();
    }

}