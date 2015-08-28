package com.example.medionchou.tobacco;

/**
 * Created by Medion on 2015/8/28.
 */
public class ProductInfo {
    private String date = "";
    private String action = "";
    private String productId = "";
    private String productName = "";
    private String quantity = "";
    private String unit = "";
    private String persion = "";

    public ProductInfo(String date, String action, String productId, String productName, String quantity, String unit, String persion) {
        this.date = date;
        this.action = action;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unit = unit;
        this.persion = persion;
    }

    public ProductInfo(String productId, String productName, String quantity, String unit) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getDate() {
        return  date;
    }

    public String getAction() {
        return action;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getPersion() {
        return persion;
    }
}
