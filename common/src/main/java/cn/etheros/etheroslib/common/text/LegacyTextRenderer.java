package cn.etheros.etheroslib.common.text;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Legacy（{@code §}）文本渲染器
 *
 * <p>把 {@link Text} 渲染成任意版本都可发送的字符串；
 * RGB 颜色按 {@link HexMode} 降级，供 Adventure 不可用的服务端使用
 *
 * @author 西博卡兹
 * @since 2.2.0
 */
public final class LegacyTextRenderer {

    /** RGB 颜色降级策略 */
    public enum HexMode {

        /** 取 RGB 距离最接近的 16 种命名颜色 */
        NEAREST,

        /** 输出 {@code §x§r§r§g§g§b§b}，供 Adventure legacy 序列化器解析 */
        UNUSUAL,

        /** 丢弃 RGB 颜色，使用默认颜色 */
        DROP
    }

    private LegacyTextRenderer() {
    }

    /** 渲染为 legacy 字符串，RGB 取最接近的命名颜色 */
    public static String render(Text text) {
        return render(text, HexMode.NEAREST);
    }

    /**
     * 渲染为 legacy 字符串
     *
     * @param text 文本
     * @param mode RGB 颜色降级策略
     */
    public static String render(Text text, HexMode mode) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(mode, "mode");
        StringBuilder builder = new StringBuilder();
        TextColor currentColor = null;
        EnumSet<TextDecoration> currentDecorations = EnumSet.noneOf(TextDecoration.class);
        for (Text.Segment segment : text.segments()) {
            String content = segment.content().replace('\u00A7', '&');
            if (content.isEmpty()) {
                continue;
            }
            TextColor targetColor = segment.color();
            Set<TextDecoration> targetDecorations = segment.decorations();
            boolean sameColor = Objects.equals(currentColor, targetColor);
            boolean keepDecorations = targetDecorations.containsAll(currentDecorations);
            boolean styled = currentColor != null || !currentDecorations.isEmpty();
            if (styled && (!sameColor || !keepDecorations)) {
                builder.append('§').append('r');
                currentColor = null;
                currentDecorations = EnumSet.noneOf(TextDecoration.class);
            }
            if (targetColor != null && !targetColor.equals(currentColor)) {
                appendColor(builder, targetColor, mode);
                currentColor = targetColor;
            }
            for (TextDecoration decoration : targetDecorations) {
                if (!currentDecorations.contains(decoration)) {
                    builder.append('§').append(decoration.legacyCode());
                }
            }
            currentDecorations = EnumSet.noneOf(TextDecoration.class);
            currentDecorations.addAll(targetDecorations);
            builder.append(content);
        }
        if (currentColor != null || !currentDecorations.isEmpty()) {
            builder.append('§').append('r');
        }
        return builder.toString();
    }

    private static void appendColor(StringBuilder builder, TextColor color, HexMode mode) {
        if (!color.isRgb()) {
            builder.append('§').append(color.legacyCode());
            return;
        }
        if (mode == HexMode.DROP) {
            return;
        }
        if (mode == HexMode.NEAREST) {
            builder.append('§').append(color.nearestLegacy().legacyCode());
            return;
        }
        builder.append('§').append('x');
        String hex = String.format("%06x", color.rgbValue());
        for (int index = 0; index < hex.length(); index++) {
            builder.append('§').append(hex.charAt(index));
        }
    }
}
