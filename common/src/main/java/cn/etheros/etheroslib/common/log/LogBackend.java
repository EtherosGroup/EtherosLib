package cn.etheros.etheroslib.common.log;

/**
 * 日志后端：按平台品牌提供 {@link Logger} 实现。
 *
 * <ul>
 *   <li>Bukkit / BungeeCord：JDK JUL 后端，接入服务端日志体系</li>
 *   <li>Velocity：SLF4J 后端</li>
 * </ul>
 *
 * @author 西博卡兹
 * @since 1.3.0
 */
public interface LogBackend {

    /**
     * 创建（或获取）指定名字的日志器。
     *
     * @param name 日志器名
     * @return 日志器实例
     */
    Logger create(String name);
}
