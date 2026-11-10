package cn.etheros.etheroslib.common.text;

import cn.etheros.etheroslib.common.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextSenderTest {

    @Test
    void sendsLegacyRenderedText() {
        RecordingSender sender = new RecordingSender();
        sender.sendMessage(Text.of("&c&lhello"));
        assertEquals(java.util.Collections.singletonList("§c§lhello§r"), sender.messages);
    }

    @Test
    void sendsEachLineAsSeparateMessage() {
        RecordingSender sender = new RecordingSender();
        sender.sendMessage(Text.of("&aone\n&btwo"));
        assertEquals(2, sender.messages.size());
        assertEquals("§aone§r", sender.messages.get(0));
        assertEquals("§btwo§r", sender.messages.get(1));
    }

    @Test
    void ignoresEmptyText() {
        RecordingSender sender = new RecordingSender();
        sender.sendMessage(Text.empty());
        assertTrue(sender.messages.isEmpty());
    }

    private static final class RecordingSender implements CommandSender {

        private final List<String> messages = new ArrayList<String>();

        @Override
        public String name() {
            return "test";
        }

        @Override
        public boolean hasPermission(String permission) {
            return true;
        }

        @Override
        public void sendMessage(String message) {
            messages.add(message);
        }

        @Override
        public Object raw() {
            return this;
        }
    }
}
