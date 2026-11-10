package cn.etheros.etheroslib.common.text;

import java.util.Objects;

/**
 * 文本颜色
 *
 * <p>支持 Minecraft 的 16 种命名颜色与 RGB（{@link #rgb(int, int, int)}）；
 * RGB 颜色在低版本服务端渲染时按 {@link #nearestLegacy()} 降级
 *
 * @author 西博卡兹
 * @since 2.2.0
 */
public final class TextColor {

    public static final TextColor BLACK = new TextColor('0', 0x000000);
    public static final TextColor DARK_BLUE = new TextColor('1', 0x0000AA);
    public static final TextColor DARK_GREEN = new TextColor('2', 0x00AA00);
    public static final TextColor DARK_AQUA = new TextColor('3', 0x00AAAA);
    public static final TextColor DARK_RED = new TextColor('4', 0xAA0000);
    public static final TextColor DARK_PURPLE = new TextColor('5', 0xAA00AA);
    public static final TextColor GOLD = new TextColor('6', 0xFFAA00);
    public static final TextColor GRAY = new TextColor('7', 0xAAAAAA);
    public static final TextColor DARK_GRAY = new TextColor('8', 0x555555);
    public static final TextColor BLUE = new TextColor('9', 0x5555FF);
    public static final TextColor GREEN = new TextColor('a', 0x55FF55);
    public static final TextColor AQUA = new TextColor('b', 0x55FFFF);
    public static final TextColor RED = new TextColor('c', 0xFF5555);
    public static final TextColor LIGHT_PURPLE = new TextColor('d', 0xFF55FF);
    public static final TextColor YELLOW = new TextColor('e', 0xFFFF55);
    public static final TextColor WHITE = new TextColor('f', 0xFFFFFF);

    private static final TextColor[] NAMED = {
            BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY,
            DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE
    };

    private final char legacyCode;
    private final int rgb;

    private TextColor(char legacyCode, int rgb) {
        this.legacyCode = legacyCode;
        this.rgb = rgb;
    }

    /**
     * 创建 RGB 颜色
     *
     * @throws IllegalArgumentException 分量不在 0–255
     */
    public static TextColor rgb(int red, int green, int blue) {
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
            throw new IllegalArgumentException("RGB 分量必须在 0–255：" + red + "," + green + "," + blue);
        }
        return new TextColor('\0', (red << 16) | (green << 8) | blue);
    }

    /** 是否为 RGB 颜色（1.16+ 才支持） */
    public boolean isRgb() {
        return legacyCode == '\0';
    }

    /** 命名颜色对应的 § 代码；RGB 颜色返回最接近的命名颜色代码 */
    public char legacyCode() {
        return isRgb() ? nearestLegacy().legacyCode() : legacyCode;
    }

    /** RGB 颜色按 RGB 距离取最接近的命名颜色；命名颜色返回自身 */
    public TextColor nearestLegacy() {
        if (!isRgb()) {
            return this;
        }
        TextColor nearest = BLACK;
        int best = Integer.MAX_VALUE;
        for (TextColor candidate : NAMED) {
            int distance = distanceSquared(candidate.rgb, this.rgb);
            if (distance < best) {
                best = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    public int red() {
        return (rgb >> 16) & 0xFF;
    }

    public int green() {
        return (rgb >> 8) & 0xFF;
    }

    public int blue() {
        return rgb & 0xFF;
    }

    int rgbValue() {
        return rgb;
    }

    private static int distanceSquared(int first, int second) {
        int dr = ((first >> 16) & 0xFF) - ((second >> 16) & 0xFF);
        int dg = ((first >> 8) & 0xFF) - ((second >> 8) & 0xFF);
        int db = (first & 0xFF) - (second & 0xFF);
        return dr * dr + dg * dg + db * db;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TextColor)) {
            return false;
        }
        TextColor color = (TextColor) other;
        return legacyCode == color.legacyCode && rgb == color.rgb;
    }

    @Override
    public int hashCode() {
        return Objects.hash(legacyCode, rgb);
    }

    @Override
    public String toString() {
        return isRgb() ? "#" + String.format("%06X", rgb) : "§" + legacyCode;
    }
}
