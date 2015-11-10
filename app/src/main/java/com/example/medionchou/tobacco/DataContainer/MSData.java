package com.example.medionchou.tobacco.DataContainer;

/**
 * Created by Medion on 2015/11/9.
 */
public class MSData {

    private String time;
    private String rID;
    private String rName;
    private String staffID;
    private String staff;

    public MSData(String time, String rID, String rName, String staffID, String staff) {
        this.time = time;
        this.rID = rID;
        this.rName = rName;
        this.staffID = staffID;
        this.staff = staff;
    }

    public String getTime() {
        return time;
    }

    public String getrID() {
        return rID;
    }

    public String getrName() {
        return rName;
    }

    public String getStaffID() {
        return staffID;
    }

    public String getStaff() {
        return staff;
    }
}
