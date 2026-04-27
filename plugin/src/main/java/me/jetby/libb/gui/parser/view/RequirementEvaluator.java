package me.jetby.libb.gui.parser.view;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class RequirementEvaluator {

    private static final String[] OPERATORS = {">=", "<=", "!=", "==", ">", "<"};

    public static boolean meetsAll(Player player, List<String> requirements) {
        if (requirements == null || requirements.isEmpty()) return true;
        for (String req : requirements) {
            if (!evaluate(player, req)) return false;
        }
        return true;
    }

    public static boolean evaluate(Player player, String expression) {
        if (expression == null || expression.isBlank()) return true;

        String parsed = PlaceholderAPI.setPlaceholders(player, expression).trim();

        for (String op : OPERATORS) {
            int idx = parsed.indexOf(op);
            if (idx < 0) continue;

            String left = parsed.substring(0, idx).trim();
            String right = parsed.substring(idx + op.length()).trim();

            return compare(left, right, op);
        }

        return Boolean.parseBoolean(parsed);
    }

    private static boolean compare(String left, String right, String op) {
        try {
            double l = Double.parseDouble(left);
            double r = Double.parseDouble(right);
            return switch (op) {
                case "==" -> l == r;
                case "!=" -> l != r;
                case ">=" -> l >= r;
                case "<=" -> l <= r;
                case ">" -> l > r;
                case "<" -> l < r;
                default -> false;
            };
        } catch (NumberFormatException ignored) {
        }

        int cmp = left.compareToIgnoreCase(right);
        return switch (op) {
            case "==" -> cmp == 0;
            case "!=" -> cmp != 0;
            case ">=" -> cmp >= 0;
            case "<=" -> cmp <= 0;
            case ">" -> cmp > 0;
            case "<" -> cmp < 0;
            default -> false;
        };
    }
}