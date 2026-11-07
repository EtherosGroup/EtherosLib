package cn.etheros.etheroslib.common.command;

/**
 * 命令发送者抽象
 *
 * <p>平台后端把自身发送者包装成该接口；raw() 返回平台原始对象，供处理器方法按平台类型注入
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public interface CommandSender {

    String name();

    boolean hasPermission(String permission);

    void sendMessage(String message);

    Object raw();
}
