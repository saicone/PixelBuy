package com.saicone.pixelbuy.core.web.object;

import com.google.gson.annotations.SerializedName;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class WooCommerceOrder extends WordpressError {

    private Integer id;

    @SerializedName("date_created")
    private OffsetDateTime dateCreated;

    @SerializedName("meta_data")
    private List<MetaData> metaData = new ArrayList<>();

    @SerializedName("line_items")
    private List<LineItem> lineItems = new ArrayList<>();

    public Integer id() {
        return id;
    }

    public OffsetDateTime dateCreated() {
        return dateCreated;
    }

    public List<MetaData> metaData() {
        return metaData;
    }

    public List<LineItem> lineItems() {
        return lineItems;
    }

    public static class MetaData {

        private Integer id;
        private String key;
        private Object value;

        public Integer id() {
            return id;
        }

        public String key() {
            return key;
        }

        public Object value() {
            return value;
        }
    }

    public static class LineItem {

        private Integer id;
        private String name;
        @SerializedName("product_id")
        private Integer productId;
        private Integer quantity;
        private String total;

        public Integer id() {
            return id;
        }

        public String name() {
            return name;
        }

        public Integer productId() {
            return productId;
        }

        public Integer quantity() {
            return quantity;
        }

        public String total() {
            return total;
        }
    }
}
