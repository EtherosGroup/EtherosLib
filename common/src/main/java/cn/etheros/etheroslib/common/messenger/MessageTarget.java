package cn.etheros.etheroslib.common.messenger;

import cn.etheros.etheroslib.common.text.Text;

/**
 * 消息发送目标
 *
 * <p>由平台模块适配：Bukkit 包装 console sender / 玩家，Velocity 包装 CommandSource；
 * 负责把 {@link Text} 按平台能力渲染并发送
 *
 * @author 西博卡兹
 * @since 2.3.0
 */
@FunctionalInterface
public interface MessageTarget {

    void send(Text text);
}
