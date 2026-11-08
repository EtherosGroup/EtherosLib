package cn.etheros.etheroslib.common.command;

/**
 * 命令执行器
 *
 * <p>由 {@link SubCommandSpec} 声明，注册完成后每次执行命令调用一次；
 * 发送者与参数通过 {@link CommandContext} 获取
 *
 * <p>抛出 {@link CommandArgumentException} 视为参数无效，其余异常按
 * {@link CommandError#EXECUTION_ERROR} 处理
 *
 * @author 西博卡兹
 * @since 2.0.0
 */
@FunctionalInterface
public interface CommandExecutor {

    void execute(CommandContext context) throws Exception;
}
