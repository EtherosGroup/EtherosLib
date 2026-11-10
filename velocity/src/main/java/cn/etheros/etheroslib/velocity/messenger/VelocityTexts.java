package cn.etheros.etheroslib.velocity.messenger;

import cn.etheros.etheroslib.common.text.Text;
import cn.etheros.etheroslib.common.text.TextColor;
import cn.etheros.etheroslib.common.text.TextDecoration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Velocity 文本渲染
 *
 * <p>Velocity 原生提供 Adventure，直接把 {@link Text} 渲染成 {@link Component}，
 * 保留 RGB 颜色与装饰
 *
 * @author 西博卡兹
 * @since 2.3.0
 */
public final class VelocityTexts {

    private VelocityTexts() {
    }

    public static Component render(Text text) {
        Component result = Component.empty();
        for (Text.Segment segment : text.segments()) {
            if (segment.content().isEmpty()) {
                continue;
            }
            Component part = Component.text(segment.content());
            TextColor color = segment.color();
            if (color != null) {
                part = part.color(adventureColor(color));
            }
            for (TextDecoration decoration : segment.decorations()) {
                part = part.decoration(adventureDecoration(decoration), true);
            }
            result = result.append(part);
        }
        return result;
    }

    private static net.kyori.adventure.text.format.TextColor adventureColor(TextColor color) {
        if (color.isRgb()) {
            return net.kyori.adventure.text.format.TextColor.color(color.red(), color.green(), color.blue());
        }
        return namedColor(color.legacyCode());
    }

    private static NamedTextColor namedColor(char code) {
        switch (code) {
            case '0':
                return NamedTextColor.BLACK;
            case '1':
                return NamedTextColor.DARK_BLUE;
            case '2':
                return NamedTextColor.DARK_GREEN;
            case '3':
                return NamedTextColor.DARK_AQUA;
            case '4':
                return NamedTextColor.DARK_RED;
            case '5':
                return NamedTextColor.DARK_PURPLE;
            case '6':
                return NamedTextColor.GOLD;
            case '7':
                return NamedTextColor.GRAY;
            case '8':
                return NamedTextColor.DARK_GRAY;
            case '9':
                return NamedTextColor.BLUE;
            case 'a':
                return NamedTextColor.GREEN;
            case 'b':
                return NamedTextColor.AQUA;
            case 'c':
                return NamedTextColor.RED;
            case 'd':
                return NamedTextColor.LIGHT_PURPLE;
            case 'e':
                return NamedTextColor.YELLOW;
            case 'f':
                return NamedTextColor.WHITE;
            default:
                return NamedTextColor.WHITE;
        }
    }

    private static net.kyori.adventure.text.format.TextDecoration adventureDecoration(
            TextDecoration decoration) {
        switch (decoration) {
            case BOLD:
                return net.kyori.adventure.text.format.TextDecoration.BOLD;
            case ITALIC:
                return net.kyori.adventure.text.format.TextDecoration.ITALIC;
            case UNDERLINED:
                return net.kyori.adventure.text.format.TextDecoration.UNDERLINED;
            case STRIKETHROUGH:
                return net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH;
            case OBFUSCATED:
                return net.kyori.adventure.text.format.TextDecoration.OBFUSCATED;
            default:
                return net.kyori.adventure.text.format.TextDecoration.BOLD;
        }
    }
}
