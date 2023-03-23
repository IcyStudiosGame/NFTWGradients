package com.nftworlds.gradients.util;

public final class IntColor {

    private IntColor() {
    }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int fromRGB(int red, int green, int blue) {
        return (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static int fromHex(String hex) {
        if (hex.length() == 7 && hex.charAt(0) == '#') {
            return Integer.parseInt(hex.substring(1), 16);
        }
        throw new NumberFormatException();
    }

    public static String toHex(int color) {
        return "#" + String.format("%06x", color);
    }

}
