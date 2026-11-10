package cn.etheros.etheroslib.bukkit.messenger;

import cn.etheros.etheroslib.common.text.Text;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BukkitTargetsTest {

    @Test
    void sendsLegacyTextThroughBukkitSender() {
        List<String> messages = new ArrayList<String>();
        org.bukkit.command.CommandSender sender = fakeSender(messages);
        BukkitTargets.of(sender).send(Text.of("&ahi"));
        assertEquals(Collections.singletonList("§ahi§r"), messages);
    }

    @Test
    void sendsMultipleLinesAsSeparateMessages() {
        List<String> messages = new ArrayList<String>();
        org.bukkit.command.CommandSender sender = fakeSender(messages);
        BukkitTargets.of(sender).send(Text.of("&aone\n&btwo"));
        assertEquals(2, messages.size());
        assertEquals("§aone§r", messages.get(0));
        assertEquals("§btwo§r", messages.get(1));
    }

    private static org.bukkit.command.CommandSender fakeSender(final List<String> messages) {
        return (org.bukkit.command.CommandSender) Proxy.newProxyInstance(
                BukkitTargetsTest.class.getClassLoader(),
                new Class<?>[]{org.bukkit.command.CommandSender.class},
                (proxy, method, args) -> {
                    if ("sendMessage".equals(method.getName()) && args != null) {
                        for (Object argument : args) {
                            if (argument instanceof String) {
                                messages.add((String) argument);
                            } else if (argument instanceof String[]) {
                                Collections.addAll(messages, (String[]) argument);
                            }
                        }
                        return null;
                    }
                    if ("toString".equals(method.getName())) {
                        return "FakeCommandSender";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return false;
                    }
                    return null;
                });
    }
}
