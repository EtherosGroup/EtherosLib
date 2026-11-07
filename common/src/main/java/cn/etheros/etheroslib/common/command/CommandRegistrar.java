package cn.etheros.etheroslib.common.command;

/**
 * 命令注册后端
 *
 * <p>各平台实现注册与注销；命令树与解析由 common 负责
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public interface CommandRegistrar {

    void register(CommandDefinition definition);

    void unregister(String name);
}
