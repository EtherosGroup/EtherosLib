package cn.etheros.etheroslib.bukkit.command;

import cn.etheros.etheroslib.common.command.CommandSender;

import java.util.Objects;

/**
 * Bukkit 发送者包装
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
    public Object raw() {
        return sender;
    }
}
