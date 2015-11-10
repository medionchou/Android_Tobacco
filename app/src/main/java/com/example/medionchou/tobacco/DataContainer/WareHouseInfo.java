package com.example.medionchou.tobacco.DataContainer;

/**
 * Created by Medion on 2015/8/28.
 */
public class WareHouseInfo {
    private String date = "";
    private String action = "";
    private String productId = "";
    private String productName = "";
    private String quantity = "";
    private String unit = "";
    private String pallet = "";
    private String person = "";

    public WareHouseInfo(String date, String action, String productId, String productName, String quantity, String unit, String pallet, String person) {
        this.date = date;
        this.action = action;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unit = unit;
        this.pallet = pallet;
        this.person = person;
    }

    public WareHouseInfo(String productId, String productName, String quantity, String unit) {
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

    public String getPallet() {
        return pallet;
    }

    public String getPerson() {
        return person;
    }

    public boolean isProductIdMatch(WareHouseInfo info) {
        return productId.equals(info.productId);
    }
}
