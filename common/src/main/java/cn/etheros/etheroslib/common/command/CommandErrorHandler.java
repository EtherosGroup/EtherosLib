package cn.etheros.etheroslib.common.command;

/**
 * 命令错误文案提供者
 *
 * <p>返回值约定：null 表示继续回退（子命令指定 → 子命令兜底 → 命令实例默认 → 注册表默认 → 内置默认）；
 * 空串表示已处理、不再发送任何文案；其余返回原样发送给执行者
 *
 * <p>命令类可直接实现本接口，作为该命令所有子命令的默认文案来源，适合接入 i18n：
 * {@code return i18n.get("command." + context.command() + "." + context.error());}
 *
 * @author 西博卡兹
 * @since 2.0.0
 */
@FunctionalInterface
public interface CommandErrorHandler {

    /**
     * 生成错误文案
     *
     * @param context 错误上下文，字段按错误类型取用
     * @return 要发送的文案；null 继续回退；空串不发送
     */
    String message(CommandErrorContext context);
}
