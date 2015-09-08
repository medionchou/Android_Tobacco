package com.example.medionchou.tobacco.DataContainer;

/**
 * Created by Medion on 2015/9/7.
 */
public class LineState {
    private String category;
    private String line;
    private int status;
    private String statusText;

    public LineState(String category, String line, int status, String statusText) {
        if (category.equals("CM")) {
            this.category = "捲包";
        } else if (category.equals("PM")){
            this.category = "包裝";
        } else {
            this.category = "濾嘴風送機";
        }

        this.line = line;
        this.status = status;
        this.statusText = statusText;
    }

    public String getCategory() {
        return category;
    }

    public String getLineNum() {
        return line;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusText() {
        return statusText;
    }

    public String toString() {
        return category + " " + line + " " + status + " " + statusText;
    }

    public boolean isSwapLineMatch(LineState tmp) {
        return category.equals(tmp.getCategory()) && line.equals(tmp.getLineNum());
    }
}
