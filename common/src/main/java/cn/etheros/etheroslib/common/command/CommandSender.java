package cn.etheros.etheroslib.common.command;

import cn.etheros.etheroslib.common.text.Text;

import java.util.Objects;

/**
 * 命令发送者抽象
 *
 * <p>平台后端把自身发送者包装成该接口；raw() 返回平台原始对象，供处理器方法按平台类型注入
 * <p>{@link #sendMessage(Text)} 默认按 legacy {@code §} 渲染并拆分多行，
 * 平台可实现为 Component 发送
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public interface CommandSender {

    String name();

    boolean hasPermission(String permission);

    void sendMessage(String message);

    /**
     * 发送 {@link Text}
     *
     * <p>默认实现：渲染为 {@code §} 字符串，按换行拆分后逐条发送
     */
    default void sendMessage(Text text) {
        Objects.requireNonNull(text, "text");
        if (text.isEmpty()) {
            return;
        }
        for (Text line : text.lines()) {
            sendMessage(line.toLegacy());
        }
    }

    Object raw();
}
