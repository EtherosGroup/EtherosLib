package cn.etheros.etheroslib.common.command;

import java.util.Objects;

/**
 * 补全上下文默认实现
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
final class SimpleTabContext implements TabContext {

    private final CommandSender sender;
    private final String label;
    private final String[] args;
    private final int argIndex;
    private final String current;

    SimpleTabContext(CommandSender sender, String label, String[] args) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.label = label == null ? "" : label;
        this.args = args == null ? new String[0] : args.clone();
        this.argIndex = this.args.length == 0 ? 0 : this.args.length - 1;
        this.current = this.args.length == 0 ? "" : this.args[this.argIndex];
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String[] args() {
        return args;
    }

    @Override
    public int argIndex() {
        return argIndex;
    }

    @Override
    public String current() {
        return current;
    }

    @Override
    public boolean hasPermission(String permission) {
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }
}
