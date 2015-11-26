package com.example.medionchou.tobacco.DataContainer;

/**
 * Created by Medion on 2015/11/19.
 */
public class Quality {
    private String time;
    private String lineNum;
    private String product;
    private String[] weight;
    private String[] perimeter;
    private String[] breath;
    private int colorWeight;
    private int colorPerimeter;
    private int colorBreath;

    public Quality(String time, String lineNum, String product, String weightMax, String weightValue, String weightMin, String perimeterMax, String perimeterValue, String perimeterMin, String breathMax, String breathValue, String breathMin) {
        weight = new String[3];
        perimeter = new String[3];
        breath = new String[3];

        this.time = time;
        this.lineNum = lineNum;
        this.product = product;

        weight[0] = weightMax;
        weight[1] = weightValue;
        weight[2] = weightMin;

        perimeter[0] = perimeterMax;
        perimeter[1] = perimeterValue;
        perimeter[2] = perimeterMin;

        breath[0] = breathMax;
        breath[1] = breathValue;
        breath[2] = breathMin;

        colorWeight = getColor(weight);
        colorPerimeter = getColor(perimeter);
        colorBreath = getColor(breath);
    }


    public String getTime() {
        return time;
    }

    public int getLineNum() {
        return Integer.valueOf(lineNum);
    }

    public String getProduct() {
        return product;
    }

    public String[] getWeight() {
        return weight;
    }

    public String[] getPerimeter() {
        return perimeter;
    }

    public String[] getBreath() {
        return breath;
    }

    public int getColorWeight() {
        return colorWeight;
    }

    public int getColorPerimeter() {
        return colorPerimeter;
    }

    public int getColorBreath() {
        return colorBreath;
    }

    private int getColor(String[] parameter) {
        double max = Double.valueOf(parameter[0]);
        double value = Double.valueOf(parameter[1]);
        double min = Double.valueOf(parameter[2]);


        if ( (value < min && value >= (min - min*0.02)) || (value <= (max + max*0.02) && value > max))
            return 2;
        else if (value < (min - min*0.02) || value > (max + max*0.02) ) {
            return 0;
        } else {
            return 1;
        }
    }
}

