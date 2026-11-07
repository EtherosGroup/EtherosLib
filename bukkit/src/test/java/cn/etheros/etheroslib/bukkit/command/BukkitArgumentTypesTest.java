package cn.etheros.etheroslib.bukkit.command;

import cn.etheros.etheroslib.common.command.ArgumentTypes;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BukkitArgumentTypesTest {

    @Test
    void registersPlatformTypes() {
        BukkitArgumentTypes.register();
        assertNotNull(ArgumentTypes.resolve(Player.class));
        assertNotNull(ArgumentTypes.resolve(OfflinePlayer.class));
        assertNotNull(ArgumentTypes.resolve(World.class));
        assertNotNull(ArgumentTypes.resolve(Material.class));
    }

    @Test
    void parsesMaterialWithoutServer() throws Exception {
        BukkitArgumentTypes.register();
        assertEquals(Material.STONE, ArgumentTypes.resolve(Material.class).parse("STONE"));
    }
}
