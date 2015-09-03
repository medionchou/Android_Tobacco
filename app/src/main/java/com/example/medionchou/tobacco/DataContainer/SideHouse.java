package com.example.medionchou.tobacco.DataContainer;

/**
 * Created by Medion on 2015/8/31.
 */
public class SideHouse {
    String name;
    String productName;
    String quantity;

    public SideHouse(String name, String productName, String quantity) {
        this.name = name;
        this.productName = productName;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getProductName() {
        return productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public boolean isNameMatch(SideHouse sideHouse) {
        return name.equals(sideHouse.getName());
    }
}
