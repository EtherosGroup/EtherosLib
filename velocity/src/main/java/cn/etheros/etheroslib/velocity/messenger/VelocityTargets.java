package cn.etheros.etheroslib.velocity.messenger;

import cn.etheros.etheroslib.common.messenger.MessageTarget;
import cn.etheros.etheroslib.common.text.Text;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.Objects;

/**
 * Velocity 消息目标
 *
 * <p>控制台/玩家都实现 {@link CommandSource}，统一渲染成 Adventure Component 后发送；
 * 多行消息逐行发送
 *
 * @author 西博卡兹
 * @since 2.3.0
 */
public final class VelocityTargets {

    private VelocityTargets() {
    }

    /** 任意命令源（玩家、控制台等） */
    public static MessageTarget of(CommandSource source) {
        CommandSource target = Objects.requireNonNull(source, "source");
        return text -> {
            for (Text line : text.lines()) {
                target.sendMessage(VelocityTexts.render(line));
            }
        };
    }

    /** 代理端控制台 */
    public static MessageTarget console(ProxyServer proxy) {
        return of(Objects.requireNonNull(proxy, "proxy").getConsoleCommandSource());
    }
}
