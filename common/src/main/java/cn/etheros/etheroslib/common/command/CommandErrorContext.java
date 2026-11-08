package cn.etheros.etheroslib.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 命令错误上下文
 *
 * <p>框架拒绝执行或执行失败时构造，交给 {@link CommandErrorHandler} 生成文案。
 * 字段按错误类型取用，不适用的字段为空串、null 或 0：
 *
 * <ul>
 *   <li>{@link CommandError#NO_PERMISSION}：{@link #permission()}</li>
 *   <li>{@link CommandError#PLAYER_ONLY}、{@link CommandError#SENDER_MISMATCH}：{@link #requiredSenderType()}</li>
 *   <li>{@link CommandError#NO_EXECUTOR}、{@link CommandError#INVALID_ARGUMENT_COUNT}：{@link #usage()}、{@link #minArguments()}、{@link #maxArguments()}</li>
 *   <li>{@link CommandError#INVALID_ARGUMENT}：{@link #argumentName()}、{@link #argumentInput()}、{@link #detail()}</li>
 *   <li>{@link CommandError#EXECUTION_ERROR}：{@link #exception()}</li>
 * </ul>
 *
 * <p>所有错误类型都可用 {@link #command()}、{@link #label()}、{@link #path()}、{@link #args()} 拼装 i18n key
 *
 * @author 西博卡兹
 * @since 2.0.0
 */
public final class CommandErrorContext {

    private final CommandError error;
    private final CommandSender sender;
    private final String command;
    private final String label;
    private final List<String> path;
    private final String[] args;
    private final String permission;
    private final Class<?> requiredSenderType;
    private final String usage;
    private final int minArguments;
    private final int maxArguments;
    private final String argumentName;
    private final String argumentInput;
    private final String detail;
    private final Throwable exception;

    private CommandErrorContext(CommandError error, CommandSender sender, String command, String label,
                                List<String> path, String[] args, String permission, Class<?> requiredSenderType,
                                String usage, int minArguments, int maxArguments, String argumentName,
                                String argumentInput, String detail, Throwable exception) {
        this.error = Objects.requireNonNull(error, "error");
        this.sender = Objects.requireNonNull(sender, "sender");
        this.command = command == null ? "" : command;
        this.label = label == null ? this.command : label;
        this.path = Collections.unmodifiableList(new ArrayList<String>(
                path == null ? Collections.<String>emptyList() : path));
        this.args = args == null ? new String[0] : args.clone();
        this.permission = permission == null ? "" : permission;
        this.requiredSenderType = requiredSenderType;
        this.usage = usage == null ? "" : usage;
        this.minArguments = minArguments;
        this.maxArguments = maxArguments;
        this.argumentName = argumentName == null ? "" : argumentName;
        this.argumentInput = argumentInput == null ? "" : argumentInput;
        this.detail = detail == null ? "" : detail;
        this.exception = exception;
    }

    static CommandErrorContext permissionDenied(CommandSender sender, String command, String label,
                                                String[] args, List<String> path, String permission) {
        return new CommandErrorContext(CommandError.NO_PERMISSION, sender, command, label, path, args,
                permission, null, "", 0, 0, "", "", "", null);
    }

    static CommandErrorContext senderMismatch(CommandError error, CommandSender sender, String command, String label,
                                             String[] args, List<String> path, Class<?> requiredSenderType) {
        return new CommandErrorContext(error, sender, command, label, path, args,
                "", requiredSenderType, "", 0, 0, "", "", "", null);
    }

    static CommandErrorContext usageRequired(CommandError error, CommandSender sender, String command, String label,
                                            String[] args, List<String> path, String usage, int min, int max) {
        return new CommandErrorContext(error, sender, command, label, path, args,
                "", null, usage, min, max, "", "", "", null);
    }

    static CommandErrorContext invalidArgument(CommandSender sender, String command, String label, String[] args,
                                               List<String> path, String argumentName, String argumentInput,
                                               String detail) {
        return new CommandErrorContext(CommandError.INVALID_ARGUMENT, sender, command, label, path, args,
                "", null, "", 0, 0, argumentName, argumentInput, detail, null);
    }

    static CommandErrorContext executionError(CommandSender sender, String command, String label, String[] args,
                                              List<String> path, Throwable exception) {
        return new CommandErrorContext(CommandError.EXECUTION_ERROR, sender, command, label, path, args,
                "", null, "", 0, 0, "", "", "", exception);
    }

    /** 错误类型 */
    public CommandError error() {
        return error;
    }

    /** 执行者 */
    public CommandSender sender() {
        return sender;
    }

    /** 命令名 */
    public String command() {
        return command;
    }

    /** 实际输入的命令标签，可能为别名 */
    public String label() {
        return label;
    }

    /** 已匹配的子命令路径，命令级错误为空列表 */
    public List<String> path() {
        return path;
    }

    /** 原始参数，含子命令段 */
    public String[] args() {
        return args.clone();
    }

    /** 缺失的权限；仅 {@link CommandError#NO_PERMISSION} 有值 */
    public String permission() {
        return permission;
    }

    /** 要求的发送者类型；仅 {@link CommandError#PLAYER_ONLY}、{@link CommandError#SENDER_MISMATCH} 有值 */
    public Class<?> requiredSenderType() {
        return requiredSenderType;
    }

    /** 用法文案；仅 {@link CommandError#NO_EXECUTOR}、{@link CommandError#INVALID_ARGUMENT_COUNT} 有值 */
    public String usage() {
        return usage;
    }

    /** 参数数量下限；仅 {@link CommandError#INVALID_ARGUMENT_COUNT} 有效 */
    public int minArguments() {
        return minArguments;
    }

    /** 参数数量上限；仅 {@link CommandError#INVALID_ARGUMENT_COUNT} 有效 */
    public int maxArguments() {
        return maxArguments;
    }

    /** 解析失败的参数名；仅 {@link CommandError#INVALID_ARGUMENT} 有值 */
    public String argumentName() {
        return argumentName;
    }

    /** 解析失败的原始输入；仅 {@link CommandError#INVALID_ARGUMENT} 有值 */
    public String argumentInput() {
        return argumentInput;
    }

    /** 解析失败原因；仅 {@link CommandError#INVALID_ARGUMENT} 有值 */
    public String detail() {
        return detail;
    }

    /** 执行器异常；仅 {@link CommandError#EXECUTION_ERROR} 有值 */
    public Throwable exception() {
        return exception;
    }

    /**
     * 内置默认文案
     *
     * <p>可用于在自定义文案前后拼接，如 {@code "§c" + context.defaultMessage()}
     */
    public String defaultMessage() {
        switch (error) {
            case NO_PERMISSION:
                return "无权限：" + permission;
            case PLAYER_ONLY:
                return "该命令只能由玩家执行";
            case SENDER_MISMATCH:
                return "命令执行者类型不匹配：" + (requiredSenderType == null ? "" : requiredSenderType.getSimpleName());
            case NO_EXECUTOR:
            case INVALID_ARGUMENT_COUNT:
                return usage;
            case INVALID_ARGUMENT:
                return "错误：参数无效：" + (argumentName.isEmpty() ? "" : argumentName + "：") + detail;
            case EXECUTION_ERROR:
                return "命令执行出错：" + (exception == null || exception.getMessage() == null
                        ? "" : exception.getMessage());
            default:
                return "";
        }
    }
}
