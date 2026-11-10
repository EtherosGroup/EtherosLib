package cn.etheros.etheroslib.common.text;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextTest {

    @Test
    void parsesLegacyColorsAndDecorations() {
        assertEquals("§c§lhello§r", Text.of("&c&lhello").toLegacy());
    }

    @Test
    void acceptsSectionPrefix() {
        assertEquals("§aX§r", Text.of("§aX").toLegacy());
    }

    @Test
    void resetsStyle() {
        assertEquals("§c§lX§rY", Text.of("&c&lX&rY").toLegacy());
    }

    @Test
    void keepsUnknownAmpersandSequences() {
        assertEquals("&z", Text.of("&z").toLegacy());
    }

    @Test
    void parsesRgbColorWithNearestFallback() {
        assertEquals("§cX§r", Text.of("&#FF5555X").toLegacy());
        assertEquals("§4X§r", Text.of("&#FF0000X").toLegacy());
    }

    @Test
    void rendersUnusualHexForAdventure() {
        assertTrue(Text.of("&#FF0000X").toLegacyUnusualHex().startsWith("§x§f§f§0§0§0§0X"));
    }

    @Test
    void colorAppliesToExistingTextAndAppends() {
        assertEquals("§cAB§r", Text.of("A").color(TextColor.RED).append("B").toLegacy());
    }

    @Test
    void appendingTextInheritsCurrentStyle() {
        assertEquals("§cAB§r", Text.of("&cA").append(Text.of("B")).toLegacy());
    }

    @Test
    void appendingTextKeepsExplicitStyle() {
        assertEquals("§cA§r§aB§r", Text.of("&cA").append(Text.of("&aB")).toLegacy());
    }

    @Test
    void appendStringUsesStyleFromLastSegment() {
        assertEquals("§cAB§r", Text.of("&cA").append("B").toLegacy());
    }

    @Test
    void splitsLines() {
        List<Text> lines = Text.of("&aone\n&btwo").lines();
        assertEquals(2, lines.size());
        assertEquals("§aone§r", lines.get(0).toLegacy());
        assertEquals("§btwo§r", lines.get(1).toLegacy());
    }

    @Test
    void normalizesWindowsLineEndings() {
        assertEquals("a\nb", Text.of("a\r\nb").plain());
    }

    @Test
    void emptyTextHasNoContent() {
        assertTrue(Text.empty().isEmpty());
        assertFalse(Text.of(" ").isEmpty());
        assertEquals("", Text.empty().plain());
    }

    @Test
    void plainStripsCodes() {
        assertEquals("hello world", Text.of("&ahello &lworld&r").plain());
    }

    @Test
    void parsesHexColorIntoRgbColor() {
        Text parsed = Text.of("&#12AB34x");
        TextColor color = parsed.segments().get(0).color();
        assertTrue(color != null && color.isRgb());
        assertEquals(0x12, color.red());
        assertEquals(0xAB, color.green());
        assertEquals(0x34, color.blue());
    }

    @Test
    void nearestLegacyColorMatchesClosestNamedColor() {
        assertEquals(TextColor.WHITE, TextColor.rgb(255, 255, 255).nearestLegacy());
        assertEquals(TextColor.GREEN, TextColor.rgb(0x55, 0xFF, 0x55).nearestLegacy());
    }

    @Test
    void builderChainKeepsStyleForAppendedText() {
        Text text = Text.of("&7prefix ").color(TextColor.GRAY)
                .append(Text.of("value").color(TextColor.RED).bold())
                .append(" plain");
        assertEquals("prefix value plain", text.plain());
        String legacy = text.toLegacy();
        assertTrue(legacy.contains("§7"));
        assertTrue(legacy.contains("§c"));
        assertTrue(legacy.contains("§l"));
    }
}
