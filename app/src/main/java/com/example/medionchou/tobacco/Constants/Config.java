package com.example.medionchou.tobacco.Constants;

import android.graphics.Color;

/**
 * Created by Medion on 2015/9/3.
 */
public class Config {
    public static final int TEXT_SIZE = 30;
    public static final int TEXT_TITLE_SIZE = 32;

    public static int getColor(int code) {
        switch (code) {
            case 0:
                return Color.GRAY;
            case 1:
                return Color.RED;
            case 2:
                return Color.YELLOW;
            case 3:
                return Color.GREEN;
        }

        return -1;
    }
}
