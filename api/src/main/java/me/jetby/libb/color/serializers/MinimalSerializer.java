package me.jetby.libb.color.serializers;

import me.jetby.libb.color.Serializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class MinimalSerializer implements Serializer {


    @Override
    public Component deserialize(String input) {
        if (input == null) return Component.empty();

        List<Component> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        TextColor currentColor = null;
        boolean bold = false, italic = false, underlined = false,
                strikethrough = false, obfuscated = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if ((c == '&' || c == '§') && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(i + 1));

                if (code == 'r') {
                    if (!builder.isEmpty()) {
                        components.add(buildComponent(builder.toString(), currentColor, bold, italic, underlined, strikethrough, obfuscated));
                        builder = new StringBuilder();
                    }
                    currentColor = null;
                    bold = italic = underlined = strikethrough = obfuscated = false;
                    i++;
                    continue;
                }

                if ("lnomk".indexOf(code) != -1) {
                    if (!builder.isEmpty()) {
                        components.add(buildComponent(builder.toString(), currentColor, bold, italic, underlined, strikethrough, obfuscated));
                        builder = new StringBuilder();
                    }
                    switch (code) {
                        case 'l' -> bold = true;
                        case 'o' -> italic = true;
                        case 'n' -> underlined = true;
                        case 'm' -> strikethrough = true;
                        case 'k' -> obfuscated = true;
                    }
                    i++;
                    continue;
                }

                // legacy &a, &c, etc...
                TextColor legacy = fromLegacyCode(code);
                if (legacy != null) {
                    if (!builder.isEmpty()) {
                        components.add(buildComponent(builder.toString(), currentColor, bold, italic, underlined, strikethrough, obfuscated));
                        builder = new StringBuilder();
                    }
                    currentColor = legacy;
                    bold = italic = underlined = strikethrough = obfuscated = false;
                    i++;
                    continue;
                }

            } else if (c == '#' && i + 6 < input.length()) {
                // hex #FF5500
                try {
                    String hex = input.substring(i + 1, i + 7);
                    TextColor color = TextColor.fromHexString("#" + hex);
                    if (color != null) {
                        if (!builder.isEmpty()) {
                            components.add(buildComponent(builder.toString(), currentColor, bold, italic, underlined, strikethrough, obfuscated));
                            builder = new StringBuilder();
                        }
                        currentColor = color;
                        bold = italic = underlined = strikethrough = obfuscated = false;
                        i += 6;
                        continue;
                    }
                } catch (Exception ignored) {}

            } else if (c == '<' && i + 8 < input.length() && input.charAt(i + 1) == '#') {
                // gradient <#FF0000>text</#00FF00>
                int closeTag = input.indexOf('>', i);
                if (closeTag != -1) {
                    String hex = input.substring(i + 1, closeTag);
                    TextColor color = TextColor.fromHexString(hex);
                    if (color != null) {
                        if (!builder.isEmpty()) {
                            components.add(buildComponent(builder.toString(), currentColor, bold, italic, underlined, strikethrough, obfuscated));
                            builder = new StringBuilder();
                        }
                        // end tag
                        int endTag = input.indexOf("</#", closeTag);
                        int endClose = endTag != -1 ? input.indexOf('>', endTag) : -1;
                        if (endTag != -1 && endClose != -1) {
                            String gradientEndHex = input.substring(endTag + 1, endClose + 1);
                            TextColor endColor = TextColor.fromHexString(gradientEndHex.substring(1));
                            String gradientText = input.substring(closeTag + 1, endTag);
                            components.add(buildGradient(gradientText, color, endColor));
                            i = endClose;
                            continue;
                        }
                        currentColor = color;
                        i = closeTag;
                        continue;
                    }
                }
            }

            builder.append(c);
        }

        if (!builder.isEmpty()) {
            components.add(buildComponent(builder.toString(), currentColor, bold, italic, underlined, strikethrough, obfuscated));
        }

        return Component.empty().children(components);
    }

    private Component buildComponent(String text, TextColor color, boolean bold, boolean italic,
                                     boolean underlined, boolean strikethrough, boolean obfuscated) {
        return Component.text(text)
                .color(color)
                .decoration(TextDecoration.BOLD, bold)
                .decoration(TextDecoration.ITALIC, italic)
                .decoration(TextDecoration.UNDERLINED, underlined)
                .decoration(TextDecoration.STRIKETHROUGH, strikethrough)
                .decoration(TextDecoration.OBFUSCATED, obfuscated);
    }

    private Component buildGradient(String text, TextColor start, TextColor end) {
        List<Component> chars = new ArrayList<>();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            float ratio = len == 1 ? 0 : (float) i / (len - 1);
            int r = (int) (start.red() + ratio * (end.red() - start.red()));
            int g = (int) (start.green() + ratio * (end.green() - start.green()));
            int b = (int) (start.blue() + ratio * (end.blue() - start.blue()));
            chars.add(Component.text(text.charAt(i)).color(TextColor.color(r, g, b)));
        }
        return Component.empty().children(chars);
    }

    private TextColor fromLegacyCode(char code) {
        return switch (code) {
            case '0' -> NamedTextColor.BLACK;
            case '1' -> NamedTextColor.DARK_BLUE;
            case '2' -> NamedTextColor.DARK_GREEN;
            case '3' -> NamedTextColor.DARK_AQUA;
            case '4' -> NamedTextColor.DARK_RED;
            case '5' -> NamedTextColor.DARK_PURPLE;
            case '6' -> NamedTextColor.GOLD;
            case '7' -> NamedTextColor.GRAY;
            case '8' -> NamedTextColor.DARK_GRAY;
            case '9' -> NamedTextColor.BLUE;
            case 'a' -> NamedTextColor.GREEN;
            case 'b' -> NamedTextColor.AQUA;
            case 'c' -> NamedTextColor.RED;
            case 'd' -> NamedTextColor.LIGHT_PURPLE;
            case 'e' -> NamedTextColor.YELLOW;
            case 'f' -> NamedTextColor.WHITE;
            default -> null;
        };
    }
}
