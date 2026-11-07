package cn.etheros.etheroslib.common.command;

/**
 * 参数解析失败
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class CommandArgumentException extends Exception {

    private static final long serialVersionUID = 1L;

    public CommandArgumentException(String message) {
        super(message);
    }
}
