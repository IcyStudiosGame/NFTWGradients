package com.nftworlds.gradients.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Cmpt {

    private static final char COLOR_CHAR = '§';

    private static final String COLOR_REGEX = "#[0-9a-fA-F]{6}";
    private static final String TAG_REGEX = "<text(?:\\s+font=\"([^\"]+)\")?(?:\\s+color=\"(" + COLOR_REGEX + ")\")?(?:\\s+gradient=\"((?:" + COLOR_REGEX + "\\s*,?\\s*)+)\")?>(.*?)</text>";

    private static final Pattern COLOR_PATTERN = Pattern.compile(COLOR_REGEX);
    private static final Pattern TAG_PATTERN = Pattern.compile(TAG_REGEX);

    private Cmpt() {
    }

    public static Component parse(String input) {
        Component component = Component.empty();

        int lastIndex = 0;

        Matcher matcher = TAG_PATTERN.matcher(input);
        while (matcher.find()) {
            component = component.append(Component.text(input.substring(lastIndex, matcher.start())));
            lastIndex = matcher.end();

            String font = matcher.group(1);
            String colorStr = matcher.group(2);
            String gradientStr = matcher.group(3);
            String text = matcher.group(4);

            Integer color = null;
            if (colorStr != null) {
                color = Integer.parseInt(colorStr.substring(1), 16);
            }

            List<Integer> colors = null;
            if (gradientStr != null) {
                colors = new ArrayList<>();
                Matcher gradientMatcher = COLOR_PATTERN.matcher(gradientStr);
                while (gradientMatcher.find()) {
                    colors.add(Integer.parseInt(gradientMatcher.group().substring(1), 16));
                }
            }

            Component textComponent = Component.empty();

            if (colors != null && colors.size() > 1) {
                textComponent = textComponent.append(gradient(text, colors));
            } else {
                Component justText = Component.text(text);
                if (color != null) {
                    justText = justText.color(TextColor.color(color));
                }
                textComponent = textComponent.append(justText);
            }

            component = component.append(textComponent);
        }

        component = component.append(Component.text(input.substring(lastIndex)));

        return component;
    }

    public static String gradientTest(String text, List<Integer> colors) {
        StringBuilder builder = new StringBuilder();

        int textLength = getTextLength(text);
        int colorCount = colors.size();
        float step = (colorCount - 1F) / (textLength - 1F);

        int colorIndex = 0;

        boolean bold = false;
        boolean italic = false;
        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        for (int index = 0; index < text.length(); index++) {
            char symbol = text.charAt(index);

            if (symbol == COLOR_CHAR) {
                int nextIndex = index + 1;
                if (nextIndex < text.length()) {
                    char test = text.charAt(nextIndex);
                    switch (test) {
                        case 'l', 'L' -> bold = true;
                        case 'o', 'O' -> italic = true;
                        case 'n', 'N' -> underlined = true;
                        case 'm', 'M' -> strikethrough = true;
                        case 'k', 'K' -> obfuscated = true;
                        default -> {
                            bold = false;
                            italic = false;
                            underlined = false;
                            strikethrough = false;
                            obfuscated = false;
                        }
                    }
                }
                index = nextIndex;
                continue;
            }

            if (symbol != ' ') {
                int fromIndex = (int) Math.floor(step * colorIndex);
                int toIndex = Math.min(fromIndex + 1, colorCount - 1);
                float progress = step * colorIndex - fromIndex;

                int fromColor = colors.get(fromIndex);
                int toColor = colors.get(toIndex);
                int color = interpolate(fromColor, toColor, progress);

                builder.append(color(IntColor.toHex(color)));

                colorIndex++;
            }

            if (bold) {
                builder.append(ChatColor.BOLD);
            }

            if (italic) {
                builder.append(ChatColor.ITALIC);
            }

            if (underlined) {
                builder.append(ChatColor.UNDERLINE);
            }

            if (strikethrough) {
                builder.append(ChatColor.STRIKETHROUGH);
            }

            if (obfuscated) {
                builder.append(ChatColor.MAGIC);
            }

            builder.append(symbol);
        }

        return builder.toString();
    }

    private static String color(String hex) {
        if (hex.startsWith("#") && hex.length() == 7) {
            StringBuilder builder = new StringBuilder("§x");

            char[] array = hex.toCharArray();
            for (int index = 1; index < array.length; index++) {
                builder.append(COLOR_CHAR).append(array[index]);
            }

            return builder.toString();
        }
        return "";
    }

    public static Component gradient(String text, List<Integer> colors) {
        Component component = Component.empty();

        int textLength = getTextLength(text);
        int colorCount = colors.size();
        float step = (colorCount - 1F) / (textLength - 1F);

        int colorIndex = 0;

        boolean bold = false;
        boolean italic = false;
        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        for (int index = 0; index < text.length(); index++) {
            char symbol = text.charAt(index);

            Component symbolComponent = Component.text(symbol);

            if (symbol == COLOR_CHAR) {
                int nextIndex = index + 1;
                if (nextIndex < text.length()) {
                    char test = text.charAt(nextIndex);
                    switch (test) {
                        case 'l', 'L' -> bold = true;
                        case 'o', 'O' -> italic = true;
                        case 'n', 'N' -> underlined = true;
                        case 'm', 'M' -> strikethrough = true;
                        case 'k', 'K' -> obfuscated = true;
                        default -> {
                            bold = false;
                            italic = false;
                            underlined = false;
                            strikethrough = false;
                            obfuscated = false;
                        }
                    }
                }
                index = nextIndex;
                continue;
            }

            if (bold) {
                symbolComponent = symbolComponent.decoration(TextDecoration.BOLD, true);
            }

            if (italic) {
                symbolComponent = symbolComponent.decoration(TextDecoration.ITALIC, true);
            }

            if (underlined) {
                symbolComponent = symbolComponent.decoration(TextDecoration.UNDERLINED, true);
            }

            if (strikethrough) {
                symbolComponent = symbolComponent.decoration(TextDecoration.STRIKETHROUGH, true);
            }

            if (obfuscated) {
                symbolComponent = symbolComponent.decoration(TextDecoration.OBFUSCATED, true);
            }

            if (symbol != ' ') {
                int fromIndex = (int) Math.floor(step * colorIndex);
                int toIndex = Math.min(fromIndex + 1, colorCount - 1);
                float progress = step * colorIndex - fromIndex;

                int fromColor = colors.get(fromIndex);
                int toColor = colors.get(toIndex);
                int color = interpolate(fromColor, toColor, progress);

                symbolComponent = symbolComponent.color(TextColor.color(color));

                colorIndex++;
            }

            component = component.append(symbolComponent);
        }

        return component;
    }

    private static int interpolate(int fromColor, int toColor, float progress) {
        int fromR = IntColor.getRed(fromColor);
        int fromG = IntColor.getGreen(fromColor);
        int fromB = IntColor.getBlue(fromColor);

        int toR = IntColor.getRed(toColor);
        int toG = IntColor.getGreen(toColor);
        int toB = IntColor.getBlue(toColor);

        int r = Math.round(fromR + progress * (toR - fromR));
        int g = Math.round(fromG + progress * (toG - fromG));
        int b = Math.round(fromB + progress * (toB - fromB));

        return IntColor.fromRGB(r, g, b);
    }

    private static int getTextLength(String text) {
        int length = 0;

        char[] array = text.toCharArray();
        for (int index = 0; index < array.length; index++) {
            char symbol = array[index];
            if (symbol == ' ') {
                continue;
            }

            if (symbol == COLOR_CHAR) {
                index++;
                continue;
            }

            length++;
        }

        return length;
    }

}
