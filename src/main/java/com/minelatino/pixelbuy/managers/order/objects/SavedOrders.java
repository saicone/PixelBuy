package com.minelatino.pixelbuy.managers.order.objects;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SavedOrders {

    @SerializedName("processedOrders")
    @Expose
    private List<Integer> savedOrders;

    public SavedOrders(List<Integer> savedOrders) {
        this.savedOrders = savedOrders;
    }

    public List<Integer> getSavedOrders() {
        return savedOrders;
    }

    public void setSavedOrders(List<Integer> savedOrders) {
        this.savedOrders = savedOrders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("processedOrders", savedOrders).toString();
    }

}