package cn.etheros.etheroslib.common.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 命令测试用发送者
 *
 * <p>raw 可替换，用于验证平台原始发送者注入
 */
final class FakeCommandSender implements CommandSender {

    final Set<String> permissions = new HashSet<String>();
    final List<String> messages = new ArrayList<String>();
    Object raw = this;

    @Override
    public String name() {
        return "fake";
    }

    @Override
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    @Override
    public void sendMessage(String message) {
        messages.add(message);
    }

    @Override
    public Object raw() {
        return raw;
    }
}
