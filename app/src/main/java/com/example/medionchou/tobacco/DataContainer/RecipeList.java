package com.example.medionchou.tobacco.DataContainer;

/**
 * Created by Medion on 2015/11/5.
 */
public class RecipeList {

    private String serialNum;
    private String productName;

    public RecipeList(String num, String name) {
        serialNum = num;
        productName = name;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public String getProductName() {
        return productName;
    }

    public String toString() {
        return serialNum + " " + productName;
    }
}
