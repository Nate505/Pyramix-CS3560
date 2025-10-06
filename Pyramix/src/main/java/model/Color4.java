package model;

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
}