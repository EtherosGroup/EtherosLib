package cn.etheros.etheroslib.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EtherosLibCoreTest {

    @AfterEach
    void tearDown() {
        EtherosLibCore.get().clearServices();
    }

    @Test
    void initializeBindsPlatformAndVersion() {
        EtherosLibCore core = EtherosLibCore.get();

        core.initialize(Platform.VELOCITY, "1.0-SNAPSHOT");

        assertTrue(core.isInitialized());
        assertEquals(Platform.VELOCITY, core.getPlatform());
        assertEquals("1.0-SNAPSHOT", core.getVersion());
    }

    @Test
    void serviceRegistryReturnsRegisteredInstance() {
        EtherosLibCore core = EtherosLibCore.get();
        Runnable service = () -> {
        };

        core.registerService(Runnable.class, service);

        assertSame(service, core.getService(Runnable.class));
        assertSame(service, core.requireService(Runnable.class));
    }

    @Test
    void requireServiceFailsForMissingService() {
        assertThrows(IllegalStateException.class,
                () -> EtherosLibCore.get().requireService(Runnable.class));
    }
}
