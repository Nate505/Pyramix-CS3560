package model;
import java.util.ArrayList;
import java.util.List;

public enum Move {
    R, R_PRIME, L, L_PRIME, U, U_PRIME, B, B_PRIME;

    public static Move fromToken(String t) {
        t = t.trim();
        return switch (t) {
            case "R" -> R;
            case "R'" -> R_PRIME;
            case "L" -> L;
            case "L'" -> L_PRIME;
            case "U" -> U;
            case "U'" -> U_PRIME;
            case "B" -> B;
            case "B'" -> B_PRIME;
            default -> throw new IllegalArgumentException("Unknown move: " + t);
        };
    }

    public static List<Move> parseSequence(String seq) {
        String[] tokens = seq.trim().split("\\s+");
        List<Move> out = new ArrayList<>();
        for (String tok : tokens) {
            if (tok.isBlank()) continue;
            out.add(fromToken(tok));
        }
        return out;
    }
}