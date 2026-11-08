package cn.etheros.etheroslib.common.log;

import cn.etheros.etheroslib.common.log.backend.JdkLogBackend;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUL 后端的等级映射与 debug 开关。
 */
class JdkLogBackendTest {

    @Test
    void testLevelMapping() {
        java.util.logging.Logger jul = java.util.logging.Logger.getLogger("TestLib.Jdk");
        List<LogRecord> records = new ArrayList<LogRecord>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        Level originalLevel = jul.getLevel();
        jul.addHandler(handler);
        jul.setLevel(Level.ALL);
        try {
            Logger logger = new JdkLogBackend().create("TestLib.Jdk");
            logger.info("info");
            logger.warn("warn");
            logger.error("error");
            logger.debug("debug");

            assertTrue(logger.isDebugEnabled());
            assertEquals(4, records.size());
            assertEquals(Level.INFO, records.get(0).getLevel());
            assertEquals(Level.WARNING, records.get(1).getLevel());
            assertEquals(Level.SEVERE, records.get(2).getLevel());
            assertEquals(Level.FINE, records.get(3).getLevel());
        } finally {
            jul.removeHandler(handler);
            jul.setLevel(originalLevel);
        }
    }

    @Test
    void testDebugDisabledAtInfoLevel() {
        java.util.logging.Logger jul = java.util.logging.Logger.getLogger("TestLib.JdkInfo");
        Logger logger = new JdkLogBackend().create("TestLib.JdkInfo");

        jul.setLevel(Level.INFO);
        try {
            assertFalse(logger.isDebugEnabled());
        } finally {
            jul.setLevel(null);
        }
    }
}
