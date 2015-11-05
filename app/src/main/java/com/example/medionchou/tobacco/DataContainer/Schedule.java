package com.example.medionchou.tobacco.DataContainer;

/**
 * Created by Medion on 2015/11/4.
 */
public class Schedule {
    private boolean isOn;
    private String office;
    private String production;
    private String staff;

    public Schedule(boolean isOn, String office, String production, String staff) {
        this.isOn = isOn;
        this.office = office;
        this.production = production;
        this.staff = staff;
    }


    public String toString() {
        return isOn + " " + production + " " + office + " " + staff;
    }

    public boolean getOn() {
        return isOn;
    }

    public String getProduction() {
        return production;
    }

    public String getOffice() {
        return office;
    }

    public String getStaff() {
        return staff;
    }
}
