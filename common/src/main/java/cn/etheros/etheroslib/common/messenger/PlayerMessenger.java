package cn.etheros.etheroslib.common.messenger;

import cn.etheros.etheroslib.common.text.Text;

import java.util.Arrays;
import java.util.Objects;

/**
 * 玩家消息
 *
 * <p>只拼接前缀（不带等级标签），正文支持 & 颜色码 / RGB / {@link Text}，
 * 交给平台目标按能力渲染（Paper 走 Component，其余版本走 legacy §）
 *
 * <p>首个参数为 {@link Boolean} 时视为 {@code enablePrefix}（缺省 true），
 * 例如 {@code player(player).send(false, "静默提示")}
 *
 * @author 西博卡兹
 * @since 2.3.0
 */
public final class PlayerMessenger {

    private final Messenger messenger;
    private final MessageTarget target;

    PlayerMessenger(Messenger messenger, MessageTarget target) {
        this.messenger = messenger;
        this.target = Objects.requireNonNull(target, "target");
    }

    public void send(Object... messages) {
        if (messages == null || messages.length == 0) {
            return;
        }
        boolean enablePrefix = true;
        Object[] content = messages;
        if (content[0] instanceof Boolean) {
            enablePrefix = (Boolean) content[0];
            content = Arrays.copyOfRange(content, 1, content.length);
        }
        if (content.length == 0) {
            return;
        }
        Text body = MessageFormats.format(content, false);
        if (body.isEmpty()) {
            return;
        }
        target.send(Text.concat(messenger.prefix(enablePrefix), body));
    }

    @Override
    public String toString() {
        return "PlayerMessenger";
    }
}
