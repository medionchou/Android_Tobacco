package com.example.medionchou.tobacco.DataContainer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Medion on 2015/9/7.
 */
public class ProductLine implements Parcelable {

    private String category;
    private String line;
    private String[] productId;
    private String[] productName;
    private String[] current;
    private String[] left;
    private String[] total;
    int size;

    public ProductLine(String category, String line, int size) {
        this.category = category;
        this.line = line;
        this.size = size;
        if (size > 0) {
            productId = new String[size];
            productName = new String[size];
            current = new String[size];
            left = new String[size];
            total = new String[size];
        }
    }

    public ProductLine(Parcel source) {
        this.category = source.readString();
        this.line = source.readString();
        productId = source.createStringArray();
        productName = source.createStringArray();
        current = source.createStringArray();
        left = source.createStringArray();
        total = source.createStringArray();
        this.size = source.readInt();
    }

    public void setProductId(String id, int index) {
        productId[index] = id;
    }

    public void setProductName(String name, int index) {
        productName[index] = name;
    }

    public void setCurrent(String cur, int index) {
        current[index] = cur;
    }

    public void setLeft(String left, int index) {
        this.left[index] = left;
    }

    public void setTotal(String total, int index) {
        this.total[index] = total;
    }

    public int getSize() {
        return size;
    }

    public String getCategory() {
        return category;
    }

    public String getLineNum() {
        return line;
    }

    public String getProductId(int index) {
        return productId[index];
    }

    public String getProductName(int index) {
        return productName[index];
    }

    public String getCurrent(int index) {
        return current[index];
    }

    public String getLeft(int index) {
        return left[index];
    }

    public String getTotal(int index) {
        return total[index];
    }

    public boolean isProductionLineMatch(ProductLine tmp) {
        return category.equals(tmp.getCategory()) && line.equals(tmp.getLineNum());
    }

    public String toString() {
        return category + " " + line;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(line);
        dest.writeStringArray(productId);
        dest.writeStringArray(productName);
        dest.writeStringArray(current);
        dest.writeStringArray(left);
        dest.writeStringArray(total);
        dest.writeInt(size);
    }

    public static final Parcelable.Creator<ProductLine> CREATOR = new Parcelable.Creator<ProductLine>() {

        @Override
        public ProductLine createFromParcel(Parcel source) {
            return new ProductLine(source);
        }

        @Override
        public ProductLine[] newArray(int size) {
            return new ProductLine[size];
        }
    };
}
