package model;

import java.util.ArrayList;
import java.util.List;

public enum Move {
    R, R_PRIME, L, L_PRIME, U, U_PRIME;

    public static Move fromToken(String t){
        if (t == null) throw new IllegalArgumentException("Null token");
        String s = t.trim().toUpperCase();

        return switch (s) {
            // R moves
            case "R" -> R;
            case "R'", "R_PRIME", "RPRIME" -> R_PRIME;

            // L moves
            case "L" -> L;
            case "L'", "L_PRIME", "LPRIME" -> L_PRIME;

            // U moves
            case "U" -> U;
            case "U'", "U_PRIME", "UPRIME" -> U_PRIME;

            default -> throw new IllegalArgumentException("Unknown move token: \"" + t + "\"");
        };
    }

    public static List<Move> parseSequence(String seq){
        List<Move> out = new ArrayList<>();
        if (seq == null || seq.isBlank()) return out;

        String[] tokens = seq.trim().split("\\s+");
        for(String tok : tokens){
            if(tok == null || tok.isBlank()) continue;
            out.add(fromToken(tok));
        }
        return out;
    }
}
