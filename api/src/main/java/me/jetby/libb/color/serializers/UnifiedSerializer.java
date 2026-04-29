package me.jetby.libb.color.serializers;

import me.jetby.libb.color.Serializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnifiedSerializer implements Serializer {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
            "<&?#([0-9a-fA-F]{6})>([^<]*)</&?#([0-9a-fA-F]{6})>"
    );

    private static final Pattern HEX_PATTERN = Pattern.compile(
            "(?i)(?<!<)&?#([0-9a-fA-F]{6})(?![^<]*>)"
    );

    private static final String[] LEGACY_FROM = {
            "&0","&1","&2","&3","&4","&5","&6","&7","&8","&9",
            "&a","&b","&c","&d","&e","&f",
            "&A","&B","&C","&D","&E","&F",
            "&l","&n","&o","&m","&k","&r"
    };
    private static final String[] LEGACY_TO = {
            "<black>","<dark_blue>","<dark_green>","<dark_aqua>","<dark_red>","<dark_purple>",
            "<gold>","<gray>","<dark_gray>","<blue>","<green>","<aqua>","<red>",
            "<light_purple>","<yellow>","<white>",
            "<green>","<aqua>","<red>","<light_purple>","<yellow>","<white>",
            "<bold>","<underlined>","<italic>","<strikethrough>","<obfuscated>","<reset>"
    };

    @Override
    public Component deserialize(String input) {
        if (input == null || input.isEmpty()) return Component.empty();
        return miniMessage.deserialize("<!i>" + toMiniCompatible(input));
    }

    private String toMiniCompatible(String input) {
        input = input.replace("§", "&");

        input = convertOldGradient(input);

        input = HEX_PATTERN.matcher(input).replaceAll("<#$1>");

        input = replaceLegacy(input);

        return input;
    }

    private String convertOldGradient(String input) {
        if (!input.contains("<#") && !input.contains("<&#")) return input;

        Matcher matcher = GRADIENT_PATTERN.matcher(input);
        if (!matcher.find()) return input;

        StringBuilder result = new StringBuilder(input.length() + 32);
        matcher.reset();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(
                    "<gradient:#" + matcher.group(1) + ":#" + matcher.group(3) + ">"
                            + matcher.group(2) + "</gradient>"
            ));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String replaceLegacy(String input) {
        if (!input.contains("&")) return input;

        StringBuilder sb = new StringBuilder(input.length() + 16);
        int len = input.length();

        outer:
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c == '&' && i + 1 < len) {
                char next = input.charAt(i + 1);
                for (int j = 0; j < LEGACY_FROM.length; j++) {
                    if (LEGACY_FROM[j].charAt(1) == next) {
                        sb.append(LEGACY_TO[j]);
                        i++;
                        continue outer;
                    }
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}