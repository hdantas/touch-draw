package com.bignerdranch.android.draganddraw;

/**
 * Created by hdantas on 28/10/14.
 * Model class to hold the specific colors used to draw boxes.
 */

class DrawableColor{
    public static final int RED = R.color.red;
    public static final int GREEN = R.color.green;
    public static final int BLUE = R.color.blue;
    public static final int YELLOW = R.color.Gold;
    public static final int ORANGE = R.color.orange;
    public static final int PURPLE = R.color.purple;
    public static final int WHITE = R.color.white;

    public static final int BACKGROUND_COLOR = WHITE;

    public static String toString(int color) {
        switch (color){
            case RED: return "RED";
            case GREEN: return "GREEN";
            case BLUE: return "BLUE";
            case YELLOW: return "YELLOW";
            case ORANGE: return "ORANGE";
            case PURPLE: return "PURPLE";
            case WHITE: return "WHITE";
        }
        return "NO COLOR";
    }
}