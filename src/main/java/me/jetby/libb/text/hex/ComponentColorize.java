package me.jetby.libb.text.hex;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing colored text with support for legacy codes, HEX colors, and gradients.
 * <p>
 * Supported formats:
 * <ul>
 *   <li>Legacy color codes: {@code &6Text} (orange text)</li>
 *   <li>HEX colors: {@code &#FF5733Text} (custom color)</li>
 *   <li>Gradients: {@code <#FF0000>Text</#00FF00>} (red to green gradient)</li>
 *   <li>Formatting: {@code &l} (bold), {@code &n} (underline), {@code &o} (italic), {@code &m} (strikethrough), {@code &k} (obfuscated)</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class ComponentColorize {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<#([0-9a-fA-F]{6})>(.*?)</#([0-9a-fA-F]{6})>");
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?=&(?:[0-9a-fk-or]|#[0-9a-fA-F]{6}))");

    private static final ConcurrentHashMap<String, Component> CACHE = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 500;

    /**
     * Converts a list of strings to colored components.
     *
     * @param strings the list of strings to colorize
     * @return list of colored components
     */
    public static List<Component> build(List<String> strings) {
        List<Component> components = new ArrayList<>();
        for (String str : strings) {
            components.add(build(str));
        }
        return components;
    }

    /**
     * Converts a string to a colored component with support for gradients and color codes.
     * Automatically prepends {@code &r} to reset formatting.
     * Results are cached for performance.
     *
     * @param string the string to colorize
     * @return the colored component
     */
    public static Component build(String string) {
        Component cached = CACHE.get(string);
        if (cached != null) {
            return cached;
        }

        Component result = parse(string);

        if (CACHE.size() < MAX_CACHE_SIZE) {
            CACHE.put(string, result);
        }

        return result;
    }

    /**
     * Clears the component cache. Useful for reloading configs.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Parses a string without using cache.
     *
     * @param string the string to parse
     * @return the colored component
     */
    private static Component parse(String string) {
        Component component = Component.empty()
                .decoration(TextDecoration.ITALIC, false);


        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(string);

        int lastEnd = 0;
        while (gradientMatcher.find()) {
            if (gradientMatcher.start() > lastEnd) {
                String beforeText = string.substring(lastEnd, gradientMatcher.start());
                component = component.append(parseSimpleColors(beforeText));
            }

            String startHex = gradientMatcher.group(1);
            String gradientText = gradientMatcher.group(2);
            String endHex = gradientMatcher.group(3);

            component = component.append(createGradient(gradientText, startHex, endHex));

            lastEnd = gradientMatcher.end();
        }

        if (lastEnd < string.length()) {
            component = component.append(parseSimpleColors(string.substring(lastEnd)));
        }

        return component;
    }

    /**
     * Creates a gradient component by interpolating colors between start and end hex values.
     *
     * @param text     the text to apply gradient to
     * @param startHex starting color in hex format (RRGGBB)
     * @param endHex   ending color in hex format (RRGGBB)
     * @return component with gradient applied
     */
    private static Component createGradient(String text, String startHex, String endHex) {
        Set<TextDecoration> decorations = new HashSet<>();
        String clean = text;

        if (text.contains("&l")) {
            decorations.add(TextDecoration.BOLD);
            clean = clean.replace("&l", "");
        }
        if (text.contains("&n")) {
            decorations.add(TextDecoration.UNDERLINED);
            clean = clean.replace("&n", "");
        }
        if (text.contains("&o")) {
            decorations.add(TextDecoration.ITALIC);
            clean = clean.replace("&o", "");
        }
        if (text.contains("&m")) {
            decorations.add(TextDecoration.STRIKETHROUGH);
            clean = clean.replace("&m", "");
        }
        if (text.contains("&k")) {
            decorations.add(TextDecoration.OBFUSCATED);
            clean = clean.replace("&k", "");
        }

        int len = clean.length();
        int startR = Integer.parseInt(startHex.substring(0, 2), 16);
        int startG = Integer.parseInt(startHex.substring(2, 4), 16);
        int startB = Integer.parseInt(startHex.substring(4, 6), 16);
        int endR = Integer.parseInt(endHex.substring(0, 2), 16);
        int endG = Integer.parseInt(endHex.substring(2, 4), 16);
        int endB = Integer.parseInt(endHex.substring(4, 6), 16);

        StringBuilder builder = new StringBuilder();
        TextColor lastColor = null;
        Component result = Component.empty();

        for (int i = 0; i < len; i++) {
            float ratio = len > 1 ? (float) i / (len - 1) : 0;
            int r = (int) (startR + (endR - startR) * ratio);
            int g = (int) (startG + (endG - startG) * ratio);
            int b = (int) (startB + (endB - startB) * ratio);
            TextColor color = TextColor.color(r, g, b);

            if (lastColor != null && !color.equals(lastColor)) {
                Component part = Component.text(builder.toString()).color(lastColor);
                for (TextDecoration d : decorations) part = part.decorate(d);
                result = result.append(part);
                builder.setLength(0);
            }

            builder.append(clean.charAt(i));
            lastColor = color;
        }

        if (!builder.isEmpty() && lastColor != null) {
            Component part = Component.text(builder.toString()).color(lastColor);
            for (TextDecoration d : decorations) part = part.decorate(d);
            result = result.append(part);
        }

        return result;
    }

    /**
     * Parses legacy color codes (&0-f, &a-f) and HEX colors (&#RRGGBB).
     *
     * @param string the string to parse
     * @return component with colors applied
     */
    private static Component parseSimpleColors(String string) {
        Component component = Component.empty();

        String[] parts = string.split(COLOR_PATTERN.pattern());

        for (String part : parts) {
            if (part.startsWith("&") && part.length() > 1) {
                if (part.charAt(1) == '#' && part.length() >= 8) {
                    String hexCode = part.substring(2, 8);
                    String text = part.substring(8);

                    try {
                        TextColor color = TextColor.fromHexString("#" + hexCode);
                        Component textComponent = Component.text(text).color(color);
                        component = component.append(textComponent);
                    } catch (IllegalArgumentException e) {
                        component = component.append(Component.text(part));
                    }
                } else {
                    char code = part.charAt(1);
                    String text = part.substring(2);

                    Component textComponent = Component.text(text);

                    textComponent = switch (code) {
                        case '0' -> textComponent.color(NamedTextColor.BLACK);
                        case '1' -> textComponent.color(NamedTextColor.DARK_BLUE);
                        case '2' -> textComponent.color(NamedTextColor.DARK_GREEN);
                        case '3' -> textComponent.color(NamedTextColor.DARK_AQUA);
                        case '4' -> textComponent.color(NamedTextColor.DARK_RED);
                        case '5' -> textComponent.color(NamedTextColor.DARK_PURPLE);
                        case '6' -> textComponent.color(NamedTextColor.GOLD);
                        case '7' -> textComponent.color(NamedTextColor.GRAY);
                        case '8' -> textComponent.color(NamedTextColor.DARK_GRAY);
                        case '9' -> textComponent.color(NamedTextColor.BLUE);
                        case 'a' -> textComponent.color(NamedTextColor.GREEN);
                        case 'b' -> textComponent.color(NamedTextColor.AQUA);
                        case 'c' -> textComponent.color(NamedTextColor.RED);
                        case 'd' -> textComponent.color(NamedTextColor.LIGHT_PURPLE);
                        case 'e' -> textComponent.color(NamedTextColor.YELLOW);
                        case 'f' -> textComponent.color(NamedTextColor.WHITE);

                        case 'k' -> textComponent.decorate(TextDecoration.OBFUSCATED);
                        case 'l' -> textComponent.decorate(TextDecoration.BOLD);
                        case 'm' -> textComponent.decorate(TextDecoration.STRIKETHROUGH);
                        case 'n' -> textComponent.decorate(TextDecoration.UNDERLINED);
                        case 'o' -> textComponent.decorate(TextDecoration.ITALIC);
                        case 'r' -> textComponent.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                        default -> textComponent;
                    };

                    component = component.append(textComponent);
                }
            } else {
                component = component.append(Component.text(part));
            }
        }

        return component;
    }

}