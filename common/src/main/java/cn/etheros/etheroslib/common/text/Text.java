package cn.etheros.etheroslib.common.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 平台无关的文本
 *
 * <p>支持 legacy 颜色码解析（{@code &}/{@code §} 前缀、{@code &#RRGGBB}）与程序化样式，
 * 由各平台渲染器负责转换：支持 Adventure 的服务端渲染为 Component，其余版本降级为 {@code §} 字符串
 *
 * <pre>{@code
 * Text message = Text.of("&7认证失败：").color(TextColor.RED)
 *         .append("原因").bold()
 *         .append(Text.of("（联系管理员）").color(TextColor.GRAY));
 * sender.sendMessage(message);
 * }</pre>
 *
 * <p>实例不可变；{@link #append(String)} 继承当前样式，{@link #append(Text)} 未显式设置样式的部分继承当前样式
 *
 * @author 西博卡兹
 * @since 2.2.0
 */
public final class Text {

    private final List<Segment> segments;
    private final TextColor styleColor;
    private final Set<TextDecoration> styleDecorations;

    private Text(List<Segment> segments, TextColor styleColor, Set<TextDecoration> styleDecorations) {
        this.segments = Collections.unmodifiableList(new ArrayList<Segment>(segments));
        this.styleColor = styleColor;
        Set<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);
        decorations.addAll(styleDecorations);
        this.styleDecorations = Collections.unmodifiableSet(decorations);
    }

    /** 空文本 */
    public static Text empty() {
        return new Text(Collections.<Segment>emptyList(), null, EnumSet.noneOf(TextDecoration.class));
    }

    /**
     * 解析 legacy 文本
     *
     * <p>支持 {@code &0-9a-f} 颜色、{@code &l/&o/&n/&m/&k} 装饰、{@code &r} 重置与
     * {@code &#RRGGBB} RGB 颜色；无法识别的 {@code &} 原样保留
     */
    public static Text of(String input) {
        Objects.requireNonNull(input, "input");
        ParseResult parsed = parse(normalize(input), null, EnumSet.noneOf(TextDecoration.class));
        return new Text(parsed.segments, parsed.styleColor, parsed.styleDecorations);
    }

    /**
     * 设置颜色，覆盖已有颜色
     *
     * @param color 目标颜色，null 表示恢复默认颜色
     */
    public Text color(TextColor color) {
        List<Segment> recolored = new ArrayList<Segment>(segments.size());
        for (Segment segment : segments) {
            recolored.add(new Segment(segment.content, color, segment.decorations));
        }
        return new Text(recolored, color, styleDecorations);
    }

    /** 追加装饰（作用于已有文本与后续追加） */
    public Text decorate(TextDecoration decoration) {
        Objects.requireNonNull(decoration, "decoration");
        return decorate(Collections.singleton(decoration));
    }

    /** 追加多个装饰（作用于已有文本与后续追加） */
    public Text decorate(Set<TextDecoration> decorations) {
        Objects.requireNonNull(decorations, "decorations");
        EnumSet<TextDecoration> next = EnumSet.noneOf(TextDecoration.class);
        next.addAll(styleDecorations);
        next.addAll(decorations);
        List<Segment> decorated = new ArrayList<Segment>(segments.size());
        for (Segment segment : segments) {
            EnumSet<TextDecoration> merged = EnumSet.noneOf(TextDecoration.class);
            merged.addAll(segment.decorations);
            merged.addAll(decorations);
            decorated.add(new Segment(segment.content, segment.color, merged));
        }
        return new Text(decorated, styleColor, next);
    }

    public Text bold() {
        return decorate(TextDecoration.BOLD);
    }

    public Text italic() {
        return decorate(TextDecoration.ITALIC);
    }

    public Text underlined() {
        return decorate(TextDecoration.UNDERLINED);
    }

    public Text strikethrough() {
        return decorate(TextDecoration.STRIKETHROUGH);
    }

    public Text obfuscated() {
        return decorate(TextDecoration.OBFUSCATED);
    }

    /** 追加 legacy 文本，继承当前样式 */
    public Text append(String input) {
        Objects.requireNonNull(input, "input");
        ParseResult parsed = parse(normalize(input), styleColor, styleDecorations);
        List<Segment> merged = new ArrayList<Segment>(segments);
        merged.addAll(parsed.segments);
        return new Text(merged, parsed.styleColor, parsed.styleDecorations);
    }

    /** 追加文本，未显式设置样式的部分继承当前样式 */
    public Text append(Text other) {
        Objects.requireNonNull(other, "other");
        List<Segment> merged = new ArrayList<Segment>(segments);
        for (Segment segment : other.segments) {
            TextColor color = segment.color != null ? segment.color : styleColor;
            EnumSet<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);
            decorations.addAll(styleDecorations);
            decorations.addAll(segment.decorations);
            merged.add(new Segment(segment.content, color, decorations));
        }
        return new Text(merged, styleColor, styleDecorations);
    }

    /** 只读分段列表 */
    public List<Segment> segments() {
        return segments;
    }

    /** 是否没有任何内容 */
    public boolean isEmpty() {
        for (Segment segment : segments) {
            if (!segment.content.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** 逐行拆分，用于发送（聊天不支持换行） */
    public List<Text> lines() {
        List<Text> lines = new ArrayList<Text>();
        List<Segment> current = new ArrayList<Segment>();
        for (Segment segment : segments) {
            String content = segment.content;
            int start = 0;
            for (int index = 0; index < content.length(); index++) {
                if (content.charAt(index) != '\n') {
                    continue;
                }
                if (index > start) {
                    current.add(new Segment(content.substring(start, index), segment.color, segment.decorations));
                }
                lines.add(new Text(current, styleColor, styleDecorations));
                current = new ArrayList<Segment>();
                start = index + 1;
            }
            if (start < content.length()) {
                current.add(new Segment(content.substring(start), segment.color, segment.decorations));
            }
        }
        lines.add(new Text(current, styleColor, styleDecorations));
        return lines;
    }

    /** 纯文本内容，不含颜色码 */
    public String plain() {
        StringBuilder builder = new StringBuilder();
        for (Segment segment : segments) {
            builder.append(segment.content);
        }
        return builder.toString();
    }

    /** 渲染为 {@code §} 字符串，RGB 颜色取最接近的命名颜色 */
    public String toLegacy() {
        return LegacyTextRenderer.render(this, LegacyTextRenderer.HexMode.NEAREST);
    }

    /** 渲染为 Adventure 可解析的 {@code §x§r§r§g§g§b§b} 形式（用于 Component 转换） */
    public String toLegacyUnusualHex() {
        return LegacyTextRenderer.render(this, LegacyTextRenderer.HexMode.UNUSUAL);
    }

    @Override
    public String toString() {
        return toLegacy();
    }

    /** 文本分段：内容 + 解析后的样式；color 为 null 表示默认颜色 */
    public static final class Segment {

        private final String content;
        private final TextColor color;
        private final Set<TextDecoration> decorations;

        private Segment(String content, TextColor color, Set<TextDecoration> decorations) {
            this.content = content;
            this.color = color;
            EnumSet<TextDecoration> copy = EnumSet.noneOf(TextDecoration.class);
            if (decorations != null) {
                copy.addAll(decorations);
            }
            this.decorations = Collections.unmodifiableSet(copy);
        }

        public String content() {
            return content;
        }

        public TextColor color() {
            return color;
        }

        public Set<TextDecoration> decorations() {
            return decorations;
        }
    }

    private static final class ParseResult {

        private final List<Segment> segments;
        private final TextColor styleColor;
        private final Set<TextDecoration> styleDecorations;

        private ParseResult(List<Segment> segments, TextColor styleColor, Set<TextDecoration> styleDecorations) {
            this.segments = segments;
            this.styleColor = styleColor;
            this.styleDecorations = styleDecorations;
        }
    }

    private static ParseResult parse(String input, TextColor baseColor, Set<TextDecoration> baseDecorations) {
        List<Segment> segments = new ArrayList<Segment>();
        StringBuilder buffer = new StringBuilder();
        TextColor color = baseColor;
        EnumSet<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);
        decorations.addAll(baseDecorations);
        int index = 0;
        while (index < input.length()) {
            char current = input.charAt(index);
            boolean prefix = current == '&' || current == '§';
            if (!prefix || index + 1 >= input.length()) {
                buffer.append(current);
                index++;
                continue;
            }
            char next = input.charAt(index + 1);
            if (next == '#') {
                TextColor parsed = parseHex(input, index + 2);
                if (parsed != null) {
                    flush(segments, buffer, color, decorations);
                    color = parsed;
                    index += 8;
                    continue;
                }
                buffer.append(current);
                index++;
                continue;
            }
            char code = Character.toLowerCase(next);
            TextColor named = namedColor(code);
            if (named != null) {
                flush(segments, buffer, color, decorations);
                color = named;
                index += 2;
                continue;
            }
            TextDecoration parsedDecoration = decoration(code);
            if (parsedDecoration != null) {
                flush(segments, buffer, color, decorations);
                decorations.add(parsedDecoration);
                index += 2;
                continue;
            }
            if (code == 'r') {
                flush(segments, buffer, color, decorations);
                color = baseColor;
                decorations = EnumSet.noneOf(TextDecoration.class);
                decorations.addAll(baseDecorations);
                index += 2;
                continue;
            }
            buffer.append(current);
            index++;
        }
        flush(segments, buffer, color, decorations);
        return new ParseResult(segments, color, decorations);
    }

    private static String normalize(String input) {
        return input.replace("\r\n", "\n").replace('\r', '\n');
    }

    private static void flush(List<Segment> segments, StringBuilder buffer, TextColor color,
                              Set<TextDecoration> decorations) {
        if (buffer.length() == 0) {
            return;
        }
        segments.add(new Segment(buffer.toString(), color, decorations));
        buffer.setLength(0);
    }

    private static TextColor parseHex(String input, int start) {
        if (start + 6 > input.length()) {
            return null;
        }
        int rgb = 0;
        for (int index = 0; index < 6; index++) {
            int digit = Character.digit(input.charAt(start + index), 16);
            if (digit < 0) {
                return null;
            }
            rgb = (rgb << 4) | digit;
        }
        return TextColor.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    private static TextColor namedColor(char code) {
        switch (code) {
            case '0':
                return TextColor.BLACK;
            case '1':
                return TextColor.DARK_BLUE;
            case '2':
                return TextColor.DARK_GREEN;
            case '3':
                return TextColor.DARK_AQUA;
            case '4':
                return TextColor.DARK_RED;
            case '5':
                return TextColor.DARK_PURPLE;
            case '6':
                return TextColor.GOLD;
            case '7':
                return TextColor.GRAY;
            case '8':
                return TextColor.DARK_GRAY;
            case '9':
                return TextColor.BLUE;
            case 'a':
                return TextColor.GREEN;
            case 'b':
                return TextColor.AQUA;
            case 'c':
                return TextColor.RED;
            case 'd':
                return TextColor.LIGHT_PURPLE;
            case 'e':
                return TextColor.YELLOW;
            case 'f':
                return TextColor.WHITE;
            default:
                return null;
        }
    }

    private static TextDecoration decoration(char code) {
        switch (code) {
            case 'k':
                return TextDecoration.OBFUSCATED;
            case 'l':
                return TextDecoration.BOLD;
            case 'm':
                return TextDecoration.STRIKETHROUGH;
            case 'n':
                return TextDecoration.UNDERLINED;
            case 'o':
                return TextDecoration.ITALIC;
            default:
                return null;
        }
    }
}
