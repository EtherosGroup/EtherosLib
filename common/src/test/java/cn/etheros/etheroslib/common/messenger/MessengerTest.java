package cn.etheros.etheroslib.common.messenger;

import cn.etheros.etheroslib.common.text.Text;
import cn.etheros.etheroslib.common.text.TextColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessengerTest {

    private final RecordingTarget console = new RecordingTarget();
    private final RecordingTarget players = new RecordingTarget();

    private Messenger messenger() {
        return Messenger.builder(console).setPrefix("[T]").build();
    }

    @Test
    void consoleInfoUsesPrefixAndDefaultLabel() {
        messenger().console().info("hello");
        assertEquals(Collections.singletonList("[T][INFO ] hello"), console.legacyMessages());
    }

    @Test
    void consoleWarnAndErrorUseTheirLabels() {
        Messenger messenger = messenger();
        messenger.console().warn("w");
        messenger.console().error("e");
        assertEquals(java.util.Arrays.asList("[T][WARN ] w", "[T][ERROR] e"), console.legacyMessages());
    }

    @Test
    void labelsAreConfigurable() {
        Messenger messenger = Messenger.builder(console)
                .setPrefix("[T]")
                .setInfo("[I] ")
                .setWarn("[W] ")
                .setError("[E] ")
                .setDebug("[D] ")
                .setDebugEnabled(true)
                .build();
        messenger.console().info("a");
        messenger.console().warn("b");
        messenger.console().error("c");
        messenger.console().debug("d");
        assertEquals(java.util.Arrays.asList("[T][I] a", "[T][W] b", "[T][E] c", "[T][D] d"),
                console.legacyMessages());
    }

    @Test
    void emptyLabelRemovesLabel() {
        Messenger messenger = Messenger.builder(console).setPrefix("[T]").setInfo("").build();
        messenger.console().info("x");
        assertEquals(Collections.singletonList("[T]x"), console.legacyMessages());
    }

    @Test
    void enablePrefixFalseOnlyRemovesPrefix() {
        messenger().console().info(false, "x");
        assertEquals(Collections.singletonList("[INFO ] x"), console.legacyMessages());
    }

    @Test
    void playerMessageUsesPrefixWithoutLabel() {
        Messenger messenger = messenger();
        messenger.player(players).send("&ahi");
        assertEquals(Collections.singletonList("[T]§ahi§r"), players.legacyMessages());
    }

    @Test
    void playerMessageCanDisablePrefix() {
        Messenger messenger = messenger();
        messenger.player(players).send(false, "&ahi");
        assertEquals(Collections.singletonList("§ahi§r"), players.legacyMessages());
    }

    @Test
    void placeholdersAreReplaced() {
        messenger().console().info("{} + {} = {}", 1, 2, 3);
        assertEquals(Collections.singletonList("[T][INFO ] 1 + 2 = 3"), console.legacyMessages());
    }

    @Test
    void textArgumentsKeepColors() {
        messenger().console().info("&aOK");
        assertTrue(console.joined().contains("§aOK"));
    }

    @Test
    void textArgumentInstanceKeepsStyle() {
        messenger().console().info(Text.of("ok").color(TextColor.RED).bold());
        String legacy = console.joined();
        assertTrue(legacy.contains("§c"));
        assertTrue(legacy.contains("§l"));
    }

    @Test
    void nullArgumentRendersNull() {
        messenger().console().info((Object) null);
        assertEquals(Collections.singletonList("[T][INFO ] null"), console.legacyMessages());
    }

    @Test
    void debugIsDisabledByDefault() {
        messenger().console().debug("x");
        assertTrue(console.texts.isEmpty());
    }

    @Test
    void debugCanBeEnabled() {
        Messenger messenger = Messenger.builder(console).setDebugEnabled(true).build();
        messenger.console().debug("x");
        assertEquals(Collections.singletonList("[DEBUG] x"), console.legacyMessages());
    }

    @Test
    void emptyMessagesAreIgnored() {
        Messenger messenger = messenger();
        messenger.console().info();
        messenger.player(players).send();
        assertTrue(console.texts.isEmpty());
        assertTrue(players.texts.isEmpty());
    }

    @Test
    void consoleThrowableIncludesStackTrace() {
        messenger().console().error("failed", new IllegalStateException("boom"));
        String legacy = console.joined();
        assertTrue(legacy.contains("failed"));
        assertTrue(legacy.contains("boom"));
        assertTrue(legacy.contains("IllegalStateException"));
        assertTrue(legacy.contains("at "));
    }

    @Test
    void playerThrowableIsCompact() {
        messenger().player(players).send("failed", new IllegalStateException("boom"));
        String legacy = players.joined();
        assertTrue(legacy.contains("boom"));
        assertFalse(legacy.contains("at "));
    }

    @Test
    void prefixAndLabelsAreResolvedDynamically() {
        AtomicReference<String> prefix = new AtomicReference<String>("[A]");
        AtomicReference<String> label = new AtomicReference<String>("[I] ");
        Messenger messenger = Messenger.builder(console)
                .setPrefix(prefix::get)
                .setInfo(label::get)
                .build();
        messenger.console().info("x");
        prefix.set("[B]");
        label.set("[J] ");
        messenger.console().info("y");
        assertEquals(java.util.Arrays.asList("[A][I] x", "[B][J] y"), console.legacyMessages());
    }

    private static final class RecordingTarget implements MessageTarget {

        private final List<Text> texts = new ArrayList<Text>();

        @Override
        public void send(Text text) {
            texts.add(text);
        }

        private List<String> legacyMessages() {
            List<String> messages = new ArrayList<String>(texts.size());
            for (Text text : texts) {
                messages.add(text.toLegacy());
            }
            return messages;
        }

        private String joined() {
            return String.join("\n", legacyMessages());
        }
    }
}
