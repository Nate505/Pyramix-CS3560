package model;

import java.util.Random;

public final class Utils {
    private static final Random RNG = new Random();

    private Utils(){}

    public static int rnd(int n){
        return RNG.nextInt(n);
    }
}
