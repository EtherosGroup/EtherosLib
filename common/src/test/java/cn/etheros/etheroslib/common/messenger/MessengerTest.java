package cn.etheros.etheroslib.common.messenger;

import cn.etheros.etheroslib.common.text.Text;
import cn.etheros.etheroslib.common.text.TextColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessengerTest {

    private final RecordingTarget console = new RecordingTarget();
    private final RecordingTarget players = new RecordingTarget();

    private Messenger messenger() {
        return new Messenger.Builder().setConsole(console).setPrefix("[T]").build();
    }

    @Test
    void consoleInfoUsesPrefixAndDefaultLabel() {
        messenger().info("hello");
        assertEquals(Collections.singletonList("[T][INFO ] hello"), console.legacyMessages());
    }

    @Test
    void consoleWarnAndErrorUseTheirLabels() {
        Messenger messenger = messenger();
        messenger.warn("w");
        messenger.error("e");
        assertEquals(Arrays.asList("[T][WARN ] w", "[T][ERROR] e"), console.legacyMessages());
    }

    @Test
    void labelsAreConfigurable() {
        Messenger messenger = new Messenger.Builder()
                .setConsole(console)
                .setPrefix("[T]")
                .setInfo("[I] ")
                .setWarn("[W] ")
                .setError("[E] ")
                .setDebug("[D] ")
                .setDebugEnabled(true)
                .build();
        messenger.info("a");
        messenger.warn("b");
        messenger.error("c");
        messenger.debug("d");
        assertEquals(Arrays.asList("[T][I] a", "[T][W] b", "[T][E] c", "[T][D] d"),
                console.legacyMessages());
    }

    @Test
    void emptyLabelRemovesLabel() {
        Messenger messenger = new Messenger.Builder()
                .setConsole(console).setPrefix("[T]").setInfo("").build();
        messenger.info("x");
        assertEquals(Collections.singletonList("[T]x"), console.legacyMessages());
    }

    @Test
    void enablePrefixFalseOnlyRemovesPrefix() {
        messenger().info(false, "x");
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
        messenger().info("{} + {} = {}", 1, 2, 3);
        assertEquals(Collections.singletonList("[T][INFO ] 1 + 2 = 3"), console.legacyMessages());
    }

    @Test
    void textArgumentsKeepColors() {
        messenger().info("&aOK");
        assertTrue(console.joined().contains("§aOK"));
    }

    @Test
    void textArgumentInstanceKeepsStyle() {
        messenger().info(Text.of("ok").color(TextColor.RED).bold());
        String legacy = console.joined();
        assertTrue(legacy.contains("§c"));
        assertTrue(legacy.contains("§l"));
    }

    @Test
    void nullArgumentRendersNull() {
        messenger().info((Object) null);
        assertEquals(Collections.singletonList("[T][INFO ] null"), console.legacyMessages());
    }

    @Test
    void debugIsDisabledByDefault() {
        messenger().debug("x");
        assertTrue(console.texts.isEmpty());
    }

    @Test
    void debugCanBeEnabled() {
        Messenger messenger = new Messenger.Builder().setConsole(console).setDebugEnabled(true).build();
        messenger.debug("x");
        assertEquals(Collections.singletonList("[DEBUG] x"), console.legacyMessages());
    }

    @Test
    void emptyMessagesAreIgnored() {
        Messenger messenger = messenger();
        messenger.info();
        messenger.player(players).send();
        assertTrue(console.texts.isEmpty());
        assertTrue(players.texts.isEmpty());
    }

    @Test
    void consoleThrowableIncludesStackTrace() {
        messenger().error("failed", new IllegalStateException("boom"));
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
        Messenger messenger = new Messenger.Builder()
                .setConsole(console)
                .setPrefix(prefix::get)
                .setInfo(label::get)
                .build();
        messenger.info("x");
        prefix.set("[B]");
        label.set("[J] ");
        messenger.info("y");
        assertEquals(Arrays.asList("[A][I] x", "[B][J] y"), console.legacyMessages());
    }

    @Test
    void debugIsResolvedDynamically() {
        AtomicReference<Boolean> enabled = new AtomicReference<Boolean>(Boolean.FALSE);
        Messenger messenger = new Messenger.Builder()
                .setConsole(console)
                .setDebugEnabled(enabled::get)
                .build();
        messenger.debug("a");
        enabled.set(Boolean.TRUE);
        messenger.debug("b");
        assertEquals(Collections.singletonList("[DEBUG] b"), console.legacyMessages());
    }

    @Test
    void staticFactoryAcceptsConsoleTarget() {
        Messenger messenger = Messenger.builder(console).setPrefix("[T]").build();
        messenger.info("x");
        assertEquals(Collections.singletonList("[T][INFO ] x"), console.legacyMessages());
    }

    @Test
    void buildWithoutConsoleTargetFails() {
        assertThrows(IllegalStateException.class, () -> new Messenger.Builder().build());
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
