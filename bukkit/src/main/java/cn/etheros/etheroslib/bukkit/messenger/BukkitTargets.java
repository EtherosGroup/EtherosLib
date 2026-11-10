package cn.etheros.etheroslib.bukkit.messenger;

import cn.etheros.etheroslib.bukkit.command.BukkitCommandSender;
import cn.etheros.etheroslib.common.messenger.MessageTarget;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Objects;

/**
 * Bukkit 消息目标
 *
 * <p>包装控制台/玩家发送者，交给 {@link BukkitCommandSender} 按平台能力渲染：
 * Paper 1.16.5+ 走 Adventure Component，其余版本走 legacy §
 *
 * @author 西博卡兹
 * @since 2.3.0
 */
public final class BukkitTargets {

    private BukkitTargets() {
    }

    /** 控制台目标（保留颜色，服务端日志会自行记录纯文本） */
    public static MessageTarget console() {
        return of(Bukkit.getConsoleSender());
    }

    /** 指定发送者目标（玩家、控制台或任意 Bukkit 发送者） */
    public static MessageTarget of(CommandSender sender) {
        BukkitCommandSender wrapper = new BukkitCommandSender(Objects.requireNonNull(sender, "sender"));
        return wrapper::sendMessage;
    }
}
