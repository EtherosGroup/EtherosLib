package cn.etheros.etheroslib.common.messenger;

import cn.etheros.etheroslib.common.text.Text;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * 控制台消息
 *
 * <p>输出格式：{@code prefix + label + message}，不自动补空格；
 * 标签由 {@link Messenger.Builder#setInfo} 等配置，{@code enablePrefix} 只控制前缀
 *
 * <p>首个参数为 {@link Boolean} 时视为 {@code enablePrefix}（缺省 true），
 * 例如 {@code console().info(false, "静默提示")}
 *
 * <p>颜色原样交给平台控制台目标渲染（不做去色）；服务端日志会自行记录纯文本
 *
 * @author 西博卡兹
 * @since 2.3.0
 */
public final class ConsoleMessenger {

    private final Messenger messenger;

    ConsoleMessenger(Messenger messenger) {
        this.messenger = messenger;
    }

    public void info(Object... messages) {
        log(false, messenger::infoLabel, messages);
    }

    public void warn(Object... messages) {
        log(false, messenger::warnLabel, messages);
    }

    public void error(Object... messages) {
        log(false, messenger::errorLabel, messages);
    }

    public void debug(Object... messages) {
        log(true, messenger::debugLabel, messages);
    }

    private void log(boolean debug, Supplier<String> label, Object[] arguments) {
        if (debug && !messenger.debugEnabled()) {
            return;
        }
        if (arguments == null || arguments.length == 0) {
            return;
        }
        boolean enablePrefix = true;
        Object[] messages = arguments;
        if (messages[0] instanceof Boolean) {
            enablePrefix = (Boolean) messages[0];
            messages = Arrays.copyOfRange(messages, 1, messages.length);
        }
        if (messages.length == 0) {
            return;
        }
        Text body = MessageFormats.format(messages, true);
        if (body.isEmpty()) {
            return;
        }
        Text output = Text.concat(messenger.prefix(enablePrefix), Text.of(label.get()), body);
        messenger.consoleTarget().send(output);
    }

    @Override
    public String toString() {
        return "ConsoleMessenger";
    }
}
