package com.example.medionchou.tobacco.Constants;

import android.graphics.Color;

/**
 * Created by Medion on 2015/9/3.
 */
public class Config {
    public static final int TEXT_SIZE = 30;
    public static final int TEXT_TITLE_SIZE = 32;

    public static final int GRAY = 0;
    public static final int RED = 1;
    public static final int YELLOW = 2;
    public static final int GREEN = 3;


    public static int getColor(int code) {
        switch (code) {
            case GRAY:
                return Color.GRAY;
            case RED:
                return Color.RED;
            case YELLOW:
                return Color.YELLOW;
            case GREEN:
                return Color.GREEN;
        }

        return -1;
    }
}
