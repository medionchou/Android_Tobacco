package com.example.medionchou.tobacco.DataContainer;

import android.graphics.Color;

/**
 * Created by Medion on 2015/11/4.
 */
public class Schedule {
    private boolean isCMOn;
    private boolean isPMOn;
    private String office;
    private String staffCM;
    private String staffPM;
    private String bossCM;
    private String bossPM;


    private boolean isOfficeOn;
    private String staff;

    public Schedule(boolean isCMOn, boolean isPMOn, String office, String staffCM, String bossCM, String staffPM, String bossPM) {
        this.isCMOn = isCMOn;
        this.isPMOn = isPMOn;
        this.office = office.substring(2, office.length());
        this.staffCM = staffCM;
        this.bossCM = bossCM;
        this.staffPM = staffPM;
        this.bossPM = bossPM;
    }

    public Schedule(boolean isOfficeOn, String office, String staff) {
        this.isOfficeOn = isOfficeOn;
        this.office = office;
        this.staff = staff;
    }

    public boolean getCMOn() {
        return isCMOn;
    }

    public boolean getPMOn() {
        return isPMOn;
    }

    public boolean getOfficeOn() {
        return isOfficeOn;
    }

    public String getOffice() {
        return office;
    }

    public String getCMStaff() {
        return staffCM;
    }

    public String getCMBoss() {
        return bossCM;
    }

    public String getPMStaff() {
        return staffPM;
    }

    public String getPMBoss() {
        return bossPM;
    }

    public String getStaff() {
        return staff;
    }

    public int getColor(boolean type) {
        if (type)
            return Color.parseColor("#ff228b22");
        else
            return Color.parseColor("#ff303030");

    }
}
