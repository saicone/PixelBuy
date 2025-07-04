package com.saicone.pixelbuy.core.web.object;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class WooCommerceOrder extends WordpressError {

    private Integer id;
    @SerializedName("parent_id")
    private Integer parentId;
    private String number;

    @SerializedName("order_key")
    private String orderKey;

    @SerializedName("created_via")
    private String createdVia;

    private String version;

    private String status;

    private String currency;

    @SerializedName("date_created")
    private String dateCreated;
    @SerializedName("date_created_gmt")
    private String dateCreatedGmt;
    @SerializedName("date_modified")
    private String dateModified;
    @SerializedName("date_modified_gmt")
    private String dateModifiedGmt;

    @SerializedName("discount_total")
    private String discountTotal;
    @SerializedName("discount_tax")
    private String discountTax;
    @SerializedName("shipping_total")
    private String shippingTotal;
    @SerializedName("shipping_tax")
    private String shippingTax;
    @SerializedName("cart_tax")
    private String cartTax;
    private String total;
    @SerializedName("total_tax")
    private String totalTax;
    @SerializedName("prices_include_tax")
    private Boolean pricesIncludeTax;

    @SerializedName("customer_id")
    private Integer customerId;
    @SerializedName("customer_ip_address")
    private Integer customerIpAddress;
    @SerializedName("customer_user_agent")
    private Integer customerUserAgent;
    @SerializedName("customer_note")
    private Integer customerNote;

    private Billing billing;

    private Shipping shipping;

    @SerializedName("payment_method")
    private String paymentMethod;
    @SerializedName("payment_method_title")
    private String paymentMethodTitle;

    @SerializedName("transaction_id")
    private String transactionId;

    @SerializedName("date_paid")
    private String datePaid;
    @SerializedName("date_paid_gmt")
    private String datePaidGmt;
    @SerializedName("date_completed")
    private String dateCompleted;
    @SerializedName("date_completed_gmt")
    private String dateCompletedGmt;

    @SerializedName("cart_hash")
    private String cartHash;

    @SerializedName("meta_data")
    private List<MetaData> metaData = new ArrayList<>();

    @SerializedName("line_items")
    private List<LineItem> lineItems = new ArrayList<>();

    @SerializedName("tax_lines")
    private List<TaxLine> taxLines = new ArrayList<>();

    @SerializedName("shipping_lines")
    private List<ShippingLine> shippingLines = new ArrayList<>();

    @SerializedName("fee_lines")
    private List<FeeLine> feeLines = new ArrayList<>();

    @SerializedName("coupon_lines")
    private List<CouponLine> couponLines = new ArrayList<>();

    private List<Refund> refunds = new ArrayList<>();

    public Integer id() {
        return id;
    }

    public Integer parentId() {
        return parentId;
    }

    public String number() {
        return number;
    }

    public String orderKey() {
        return orderKey;
    }

    public String createdVia() {
        return createdVia;
    }

    public String version() {
        return version;
    }

    public String status() {
        return status;
    }

    public String currency() {
        return currency;
    }

    public String dateCreated() {
        return dateCreated;
    }

    public String dateCreatedGmt() {
        return dateCreatedGmt;
    }

    public String dateModified() {
        return dateModified;
    }

    public String dateModifiedGmt() {
        return dateModifiedGmt;
    }

    public String discountTotal() {
        return discountTotal;
    }

    public String discountTax() {
        return discountTax;
    }

    public String shippingTotal() {
        return shippingTotal;
    }

    public String shippingTax() {
        return shippingTax;
    }

    public String cartTax() {
        return cartTax;
    }

    public String total() {
        return total;
    }

    public String totalTax() {
        return totalTax;
    }

    public Boolean pricesIncludeTax() {
        return pricesIncludeTax;
    }

    public Integer customerId() {
        return customerId;
    }

    public Integer customerIpAddress() {
        return customerIpAddress;
    }

    public Integer customerUserAgent() {
        return customerUserAgent;
    }

    public Integer customerNote() {
        return customerNote;
    }

    public Billing billing() {
        return billing;
    }

    public Shipping shipping() {
        return shipping;
    }

    public String paymentMethod() {
        return paymentMethod;
    }

    public String paymentMethodTitle() {
        return paymentMethodTitle;
    }

    public String transactionId() {
        return transactionId;
    }

    public String datePaid() {
        return datePaid;
    }

    public String datePaidGmt() {
        return datePaidGmt;
    }

    public String dateCompleted() {
        return dateCompleted;
    }

    public String dateCompletedGmt() {
        return dateCompletedGmt;
    }

    public String cartHash() {
        return cartHash;
    }

    public List<MetaData> metaData() {
        return metaData;
    }

    public List<LineItem> lineItems() {
        return lineItems;
    }

    public List<TaxLine> taxLines() {
        return taxLines;
    }

    public List<ShippingLine> shippingLines() {
        return shippingLines;
    }

    public List<FeeLine> feeLines() {
        return feeLines;
    }

    public List<CouponLine> couponLines() {
        return couponLines;
    }

    public List<Refund> refunds() {
        return refunds;
    }

    public static class Billing extends Shipping {

        private String email;
        private String phone;

        public String email() {
            return email;
        }

        public String phone() {
            return phone;
        }
    }

    public static class Shipping {

        @SerializedName("first_name")
        private String firstName;
        @SerializedName("last_name")
        private String lastName;
        private String company;
        private String address1;
        private String address2;
        private String city;
        private String state;
        private String postcode;
        private String country;

        public String firstName() {
            return firstName;
        }

        public String lastName() {
            return lastName;
        }

        public String company() {
            return company;
        }

        public String address1() {
            return address1;
        }

        public String address2() {
            return address2;
        }

        public String city() {
            return city;
        }

        public String state() {
            return state;
        }

        public String postcode() {
            return postcode;
        }

        public String country() {
            return country;
        }
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

    public static class Tax {

        private Integer id;
        private String total;
        private String subtotal;

        public Integer id() {
            return id;
        }

        public String total() {
            return total;
        }

        public String subtotal() {
            return subtotal;
        }
    }

    public static class LineItem {

        private Integer id;
        private String name;
        @SerializedName("product_id")
        private Integer productId;
        @SerializedName("variation_id")
        private Integer variationId;
        private Integer quantity;
        @SerializedName("tax_class")
        private String taxClass;
        private String subtotal;
        @SerializedName("subtotal_tax")
        private String subtotalTax;
        private String total;
        @SerializedName("total_tax")
        private String totalTax;
        private List<Tax> taxes = new ArrayList<>();
        @SerializedName("meta_data")
        private List<MetaData> metaData = new ArrayList<>();
        private String sku;
        private Double price;

        public Integer id() {
            return id;
        }

        public String name() {
            return name;
        }

        public Integer productId() {
            return productId;
        }

        public Integer variationId() {
            return variationId;
        }

        public Integer quantity() {
            return quantity;
        }

        public String taxClass() {
            return taxClass;
        }

        public String subtotal() {
            return subtotal;
        }

        public String subtotalTax() {
            return subtotalTax;
        }

        public String total() {
            return total;
        }

        public String totalTax() {
            return totalTax;
        }

        public List<Tax> taxes() {
            return taxes;
        }

        public List<MetaData> metaData() {
            return metaData;
        }

        public String sku() {
            return sku;
        }

        public Double price() {
            return price;
        }
    }

    public static class TaxLine {

        private Integer id;
        @SerializedName("rate_code")
        private String rateCode;
        @SerializedName("rate_id")
        private Integer rateId;
        private String label;
        private Boolean compound;
        @SerializedName("tax_total")
        private String taxTotal;
        @SerializedName("shipping_tax_total")
        private String shippingTaxTotal;
        @SerializedName("meta_data")
        private List<MetaData> metaData = new ArrayList<>();

        public Integer id() {
            return id;
        }

        public String rateCode() {
            return rateCode;
        }

        public Integer rateId() {
            return rateId;
        }

        public String label() {
            return label;
        }

        public Boolean compound() {
            return compound;
        }

        public String taxTotal() {
            return taxTotal;
        }

        public String shippingTaxTotal() {
            return shippingTaxTotal;
        }

        public List<MetaData> metaData() {
            return metaData;
        }
    }

    public static class ShippingLine {

        private Integer id;
        @SerializedName("method_title")
        private String methodTitle;
        @SerializedName("method_id")
        private String methodId;
        private String total;
        @SerializedName("total_tax")
        private String totalTax;
        private List<Tax> taxes = new ArrayList<>();
        @SerializedName("meta_data")
        private List<MetaData> metaData = new ArrayList<>();

        public Integer id() {
            return id;
        }

        public String methodTitle() {
            return methodTitle;
        }

        public String methodId() {
            return methodId;
        }

        public String total() {
            return total;
        }

        public String totalTax() {
            return totalTax;
        }

        public List<Tax> taxes() {
            return taxes;
        }

        public List<MetaData> metaData() {
            return metaData;
        }
    }

    public static class FeeLine {

        private Integer id;
        private String name;
        @SerializedName("tax_class")
        private String taxClass;
        @SerializedName("tax_status")
        private String taxStatus;
        private String total;
        @SerializedName("total_tax")
        private String totalTax;
        private List<TaxLine> taxes = new ArrayList<>();
        @SerializedName("meta_data")
        private List<MetaData> metaData = new ArrayList<>();

        public Integer id() {
            return id;
        }

        public String name() {
            return name;
        }

        public String taxClass() {
            return taxClass;
        }

        public String taxStatus() {
            return taxStatus;
        }

        public String total() {
            return total;
        }

        public String totalTax() {
            return totalTax;
        }

        public List<TaxLine> taxes() {
            return taxes;
        }

        public List<MetaData> metaData() {
            return metaData;
        }
    }

    public static class CouponLine {

        private Integer id;
        private String code;
        private String discount;
        @SerializedName("discount_tax")
        private String discountTax;
        @SerializedName("meta_data")
        private List<MetaData> metaData = new ArrayList<>();

        public Integer id() {
            return id;
        }

        public String code() {
            return code;
        }

        public String discount() {
            return discount;
        }

        public String discountTax() {
            return discountTax;
        }

        public List<MetaData> metaData() {
            return metaData;
        }
    }

    public static class Refund {

        private Integer id;
        private String reason;
        private String total;

        public Integer id() {
            return id;
        }

        public String reason() {
            return reason;
        }

        public String total() {
            return total;
        }
    }
}
