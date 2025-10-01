package model;

import javafx.scene.paint.Color;

public enum Color4 {
    YELLOW, RED, GREEN, BLUE;
    public String shortName() {
        return switch (this) {
            case YELLOW -> "Y";
            case RED -> "R";
            case GREEN -> "G";
            case BLUE -> "B";
        };
    }
    public Color toFXColor() {
        return switch (this) {
            case YELLOW -> Color.YELLOW;
            case RED -> Color.RED;
            case GREEN -> Color.LIMEGREEN;
            case BLUE -> Color.DODGERBLUE;
        };
    }
}
