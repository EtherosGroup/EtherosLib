package cn.etheros.etheroslib.bukkit.command;

import cn.etheros.etheroslib.common.command.ArgumentType;
import cn.etheros.etheroslib.common.command.ArgumentTypes;
import cn.etheros.etheroslib.common.command.CommandArgumentException;
import cn.etheros.etheroslib.common.command.TabContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Bukkit 平台参数类型
 *
 * <p>注册 Player、OfflinePlayer、World、Material，供命令方法按类型解析与补全
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class BukkitArgumentTypes {

    private static volatile boolean registered;

    private BukkitArgumentTypes() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        ArgumentTypes.register(Player.class, new PlayerArgument());
        ArgumentTypes.register(OfflinePlayer.class, new OfflinePlayerArgument());
        ArgumentTypes.register(World.class, new WorldArgument());
        ArgumentTypes.register(Material.class, new MaterialArgument());
        registered = true;
    }

    private static boolean matches(String value, String prefix) {
        return prefix == null || prefix.isEmpty() || value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static final class PlayerArgument implements ArgumentType<Player> {

        @Override
        public Player parse(String input) throws CommandArgumentException {
            Player player = Bukkit.getPlayerExact(input);
            if (player == null) {
                throw new CommandArgumentException("玩家不存在或不在线");
            }
            return player;
        }

        @Override
        public Collection<String> suggest(TabContext context) {
            List<String> names = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (matches(player.getName(), context.current())) {
                    names.add(player.getName());
                }
            }
            return names;
        }
    }

    private static final class OfflinePlayerArgument implements ArgumentType<OfflinePlayer> {

        @Override
        public OfflinePlayer parse(String input) throws CommandArgumentException {
            OfflinePlayer player = Bukkit.getOfflinePlayer(input);
            if (player == null || player.getName() == null) {
                throw new CommandArgumentException("玩家不存在");
            }
            return player;
        }

        @Override
        public Collection<String> suggest(TabContext context) {
            List<String> names = new ArrayList<String>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                String name = player.getName();
                if (name != null && matches(name, context.current())) {
                    names.add(name);
                }
            }
            return names;
        }
    }

    private static final class WorldArgument implements ArgumentType<World> {

        @Override
        public World parse(String input) throws CommandArgumentException {
            World world = Bukkit.getWorld(input);
            if (world == null) {
                throw new CommandArgumentException("世界不存在");
            }
            return world;
        }

        @Override
        public Collection<String> suggest(TabContext context) {
            List<String> names = new ArrayList<String>();
            for (World world : Bukkit.getWorlds()) {
                if (matches(world.getName(), context.current())) {
                    names.add(world.getName());
                }
            }
            return names;
        }
    }

    private static final class MaterialArgument implements ArgumentType<Material> {

        @Override
        public Material parse(String input) throws CommandArgumentException {
            Material material = Material.matchMaterial(input);
            if (material == null) {
                throw new CommandArgumentException("材料不存在");
            }
            return material;
        }

        @Override
        public Collection<String> suggest(TabContext context) {
            List<String> names = new ArrayList<String>();
            for (Material material : Material.values()) {
                String name = material.name().toLowerCase(Locale.ROOT);
                if (matches(name, context.current())) {
                    names.add(name);
                }
            }
            return names;
        }
    }
}
