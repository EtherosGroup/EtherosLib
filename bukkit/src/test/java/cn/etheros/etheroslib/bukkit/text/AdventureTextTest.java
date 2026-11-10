package cn.etheros.etheroslib.bukkit.text;

import cn.etheros.etheroslib.common.text.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdventureTextTest {

    @Test
    void detectsAdventureOnClasspath() {
        assertTrue(AdventureText.isAvailable());
    }

    @Test
    void sendsNamedColorComponent() {
        AtomicReference<Component> received = new AtomicReference<Component>();
        Object sender = audience(received);
        assertTrue(AdventureText.send(sender, Text.of("&chi")));
        assertNotNull(received.get());
        TextComponent component = firstContent(received.get());
        assertEquals("hi", component.content());
        assertEquals(NamedTextColor.RED, component.color());
    }

    @Test
    void keepsRgbColorOnAdventurePath() {
        AtomicReference<Component> received = new AtomicReference<Component>();
        Object sender = audience(received);
        assertTrue(AdventureText.send(sender, Text.of("&#FF0000hi")));
        TextComponent component = firstContent(received.get());
        assertEquals("hi", component.content());
        assertEquals(TextColor.color(0xFF0000), component.color());
    }

    @Test
    void returnsFalseForNonAudienceSender() {
        assertFalse(AdventureText.send(new Object(), Text.of("hello")));
    }

    @Test
    void returnsFalseWhenReflectionFails() {
        AtomicReference<Component> received = new AtomicReference<Component>();
        Object sender = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Audience.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("sendMessage")) {
                        throw new IllegalStateException("boom");
                    }
                    return defaultValue(proxy, method);
                });
        assertFalse(AdventureText.send(sender, Text.of("hello")));
        assertEquals(null, received.get());
    }

    private static Object audience(AtomicReference<Component> received) {
        return Proxy.newProxyInstance(AdventureTextTest.class.getClassLoader(), new Class<?>[]{Audience.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("sendMessage") && args != null && args.length == 1
                            && args[0] instanceof Component) {
                        received.set((Component) args[0]);
                        return null;
                    }
                    return defaultValue(proxy, method);
                });
    }

    private static Object defaultValue(Object proxy, java.lang.reflect.Method method) {
        String name = method.getName();
        if (name.equals("toString")) {
            return "TestAudience";
        }
        if (name.equals("hashCode")) {
            return System.identityHashCode(proxy);
        }
        if (name.equals("equals")) {
            return false;
        }
        return null;
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
