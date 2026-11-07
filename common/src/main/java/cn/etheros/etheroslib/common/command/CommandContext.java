package cn.etheros.etheroslib.common.command;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 命令执行上下文
 *
 * <p>方法第一个参数声明为 CommandContext 时注入
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class CommandContext {

    private final CommandSender sender;
    private final String command;
    private final String label;
    private final String[] args;
    private final List<String> path;

    public CommandContext(CommandSender sender, String command, String label, String[] args, List<String> path) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.command = Objects.requireNonNull(command, "command");
        this.label = label == null ? command : label;
        this.args = args == null ? new String[0] : args.clone();
        this.path = Collections.unmodifiableList(Objects.requireNonNull(path, "path"));
    }

    public CommandSender sender() {
        return sender;
    }

    /** 平台原始发送者 */
    public Object rawSender() {
        return sender.raw();
    }

    public String command() {
        return command;
    }

    public String label() {
        return label;
    }

    /** 原始参数，含子命令段 */
    public String[] args() {
        return args;
    }

    /** 已匹配的子命令路径 */
    public List<String> path() {
        return path;
    }
}
