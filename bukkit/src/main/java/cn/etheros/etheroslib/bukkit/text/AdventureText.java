package cn.etheros.etheroslib.bukkit.text;

import cn.etheros.etheroslib.common.text.Text;

import java.lang.reflect.Method;

/**
 * Adventure 文本桥接
 *
 * <p>仅在服务端提供 Adventure（Paper 1.16.5+）时可用；
 * 通过反射调用 legacy 序列化器把 {@link Text} 转成 Component 后发送，不引入编译期依赖
 * <p>任何反射失败都返回 false，由调用方回退 legacy 字符串
 *
 * @author 西博卡兹
 * @since 2.2.0
 */
public final class AdventureText {

    private static final boolean AVAILABLE;
    private static final boolean HEX_SUPPORTED;
    private static final Object SERIALIZER;
    private static final Method DESERIALIZE;
    private static final Class<?> AUDIENCE;
    private static final Method SEND_MESSAGE;

    static {
        boolean available = false;
        boolean hex = false;
        Object serializer = null;
        Method deserialize = null;
        Class<?> audience = null;
        Method sendMessage = null;
        try {
            Class<?> component = Class.forName("net.kyori.adventure.text.Component");
            Class<?> serializerType = Class.forName(
                    "net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            Object built = null;
            boolean hexSupported = false;
            try {
                Class<?> builderType = Class.forName(
                        "net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder");
                Object builder = serializerType.getMethod("builder").invoke(null);
                builderType.getMethod("hexColors").invoke(builder);
                builderType.getMethod("useUnusualXRepeatedCharacterHexFormat").invoke(builder);
                built = builderType.getMethod("build").invoke(builder);
                hexSupported = true;
            } catch (Throwable ignored) {
                built = serializerType.getMethod("legacySection").invoke(null);
            }
            serializer = built;
            deserialize = serializerType.getMethod("deserialize", String.class);
            audience = Class.forName("net.kyori.adventure.audience.Audience");
            sendMessage = audience.getMethod("sendMessage", component);
            hex = hexSupported;
            available = true;
        } catch (Throwable ignored) {
            available = false;
        }
        AVAILABLE = available;
        HEX_SUPPORTED = hex;
        SERIALIZER = serializer;
        DESERIALIZE = deserialize;
        AUDIENCE = audience;
        SEND_MESSAGE = sendMessage;
    }

    private AdventureText() {
    }

    /** 服务端是否提供 Adventure 文本能力 */
    public static boolean isAvailable() {
        return AVAILABLE;
    }

    /**
     * 用 Adventure 发送单行文本
     *
     * @param sender 平台原始发送者（需实现 Adventure 的 Audience）
     * @return 是否发送成功；失败时调用方回退 legacy
     */
    public static boolean send(Object sender, Text line) {
        if (!AVAILABLE || !AUDIENCE.isInstance(sender)) {
            return false;
        }
        try {
            String legacy = HEX_SUPPORTED ? line.toLegacyUnusualHex() : line.toLegacy();
            Object component = DESERIALIZE.invoke(SERIALIZER, legacy);
            SEND_MESSAGE.invoke(sender, component);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
