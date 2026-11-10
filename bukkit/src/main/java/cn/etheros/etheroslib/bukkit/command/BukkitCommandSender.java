package cn.etheros.etheroslib.bukkit.command;

import cn.etheros.etheroslib.bukkit.text.AdventureText;
import cn.etheros.etheroslib.common.command.CommandSender;
import cn.etheros.etheroslib.common.text.Text;

import java.util.Objects;

/**
 * Bukkit 发送者包装
 *
 * <p>发送 {@link Text} 时优先走 Adventure Component（Paper 1.16.5+），
 * 不可用或不兼容时回退 legacy {@code §} 字符串
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class BukkitCommandSender implements CommandSender {

    private final org.bukkit.command.CommandSender sender;

    public BukkitCommandSender(org.bukkit.command.CommandSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
    }

    @Override
    public String name() {
        return sender.getName();
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public void sendMessage(Text text) {
        Objects.requireNonNull(text, "text");
        if (text.isEmpty()) {
            return;
        }
        if (AdventureText.isAvailable()) {
            boolean handled = true;
            for (Text line : text.lines()) {
                if (!AdventureText.send(sender, line)) {
                    handled = false;
                    break;
                }
            }
            if (handled) {
                return;
            }
        }
        CommandSender.super.sendMessage(text);
    }

    @Override
    public Object raw() {
        return sender;
    }
}
