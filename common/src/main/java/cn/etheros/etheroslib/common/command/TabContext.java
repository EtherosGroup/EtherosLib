package cn.etheros.etheroslib.common.command;

/**
 * 补全上下文
 *
 * <p>args 含正在补全的最后一段，argIndex 指向该段
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public interface TabContext {

    /** 命令别名 */
    String label();

    /** 全部参数，含正在补全的最后一段 */
    String[] args();

    /** 正在补全的参数下标 */
    int argIndex();

    /** 正在补全的参数已输入部分 */
    String current();

    /** 权限判断 */
    boolean hasPermission(String permission);
}
