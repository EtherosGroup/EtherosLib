package cn.etheros.etheroslib.velocity.messenger;

import cn.etheros.etheroslib.common.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VelocityTextsTest {

    @Test
    void rendersNamedColor() {
        TextComponent component = firstContent(VelocityTexts.render(Text.of("&aX")));
        assertEquals("X", component.content());
        assertEquals(NamedTextColor.GREEN, component.color());
    }

    @Test
    void rendersRgbColor() {
        TextComponent component = firstContent(VelocityTexts.render(Text.of("&#FF0000X")));
        assertEquals("X", component.content());
        assertEquals(TextColor.color(0xFF0000), component.color());
    }

    @Test
    void rendersDecoration() {
        TextComponent component = firstContent(VelocityTexts.render(Text.of("&lX")));
        assertEquals(TextDecoration.State.TRUE, component.decoration(TextDecoration.BOLD));
    }

    @Test
    void rendersMultipleSegments() {
        Component root = VelocityTexts.render(Text.of("&aA&cB"));
        assertEquals(2, root.children().size());
        TextComponent first = (TextComponent) root.children().get(0);
        TextComponent second = (TextComponent) root.children().get(1);
        assertEquals("A", first.content());
        assertEquals(NamedTextColor.GREEN, first.color());
        assertEquals("B", second.content());
        assertEquals(NamedTextColor.RED, second.color());
    }

    private static TextComponent firstContent(Component component) {
        if (component instanceof TextComponent) {
            TextComponent text = (TextComponent) component;
            if (!text.content().isEmpty()) {
                return text;
            }
        }
        for (Component child : component.children()) {
            TextComponent found = firstContent(child);
            if (found != null) {
                return found;
            }
        }
        throw new AssertionError("没有找到带内容的 TextComponent：" + component);
    }
}
