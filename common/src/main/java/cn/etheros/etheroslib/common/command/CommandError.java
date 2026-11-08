package cn.etheros.etheroslib.common.command;

/**
 * 命令错误类型
 *
 * <p>框架拒绝执行命令或执行失败时产生；{@link CommandErrorHandler} 据此给出自定义文案，
 * 每种类型都有内置默认文案（见 {@link CommandErrorContext#defaultMessage()}）
 *
 * @author 西博卡兹
 * @since 2.0.0
 */
public enum CommandError {

    /** 命令级或子命令级权限不足 */
    NO_PERMISSION,

    /** 发送者声明为平台玩家类型，执行者不是玩家 */
    PLAYER_ONLY,

    /** 声明的发送者类型与执行者不匹配（玩家类型之外） */
    SENDER_MISMATCH,

    /** 节点没有执行器：缺少子命令，只能显示用法 */
    NO_EXECUTOR,

    /** 参数数量不符：必选参数缺失或参数过多 */
    INVALID_ARGUMENT_COUNT,

    /** 参数解析失败 */
    INVALID_ARGUMENT,

    /** 执行器抛出异常 */
    EXECUTION_ERROR
}
