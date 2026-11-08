package cn.etheros.etheroslib.common.log;

import cn.etheros.etherosframework.di.core.ApplicationContext;
import cn.etheros.etherosframework.di.core.PropertyLoader;
import cn.etheros.etherosframework.di.spi.FieldInjectionProviders;
import cn.etheros.etheroslib.common.log.fixture.Anchor;
import cn.etheros.etheroslib.common.log.fixture.LoggedBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Log} 注入：扫描、命名、默认值与 @PostConstruct 时序。
 */
class LogInjectionTest {

    private final List<String> createdNames = new ArrayList<>();

    @BeforeEach
    void setUp() {
        EtherosLog.reset();
        FieldInjectionProviders.clear();
        createdNames.clear();
        EtherosLog.installBackend(new LogBackend() {
            @Override
            public Logger create(String name) {
                createdNames.add(name);
                return new TestLogger(name);
            }
        });
        FieldInjectionProviders.register(new LogFieldInjector());
    }

    @AfterEach
    void tearDown() {
        FieldInjectionProviders.clear();
        EtherosLog.reset();
    }

    @Test
    void testSupportsOnlyLogAnnotation() throws Exception {
        LogFieldInjector injector = new LogFieldInjector();

        assertTrue(injector.supports(field("logger")));
        assertTrue(injector.supports(field("databaseLogger")));
        assertFalse(injector.supports(field("plain")));
    }

    @Test
    void testInjectionUsesLoggerNameFromApplicationProperties() {
        ApplicationContext context = ApplicationContext.run(Anchor.class);
        try {
            LoggedBean bean = context.getBean(LoggedBean.class);
            assertNotNull(bean);
            assertEquals("TestLib.LoggedBean", bean.getLogger().getName());
            assertEquals("TestLib.database", bean.getDatabaseLogger().getName());
            assertEquals("TestLib.LoggedBean", bean.getLoggerNameInPostConstruct(),
                    "自定义注入应早于 @PostConstruct");
        } finally {
            context.shutdown();
        }
    }

    @Test
    void testDefaultBaseNameWhenPropertyMissing() throws Exception {
        ApplicationContext emptyContext = new ApplicationContext() {
            @Override
            public PropertyLoader getPropertyLoader() {
                // parent = null：不继承测试 classpath，读不到 application.properties
                return new PropertyLoader(new ClassLoader(null) {
                });
            }
        };

        Object resolved = new LogFieldInjector().resolve(field("logger"), "loggedBean", emptyContext);

        assertTrue(resolved instanceof Logger);
        assertEquals("Etheros.LoggedBean", ((Logger) resolved).getName());
    }

    @Test
    void testExplicitSuffixFromAnnotation() throws Exception {
        ApplicationContext context = new ApplicationContext() {
            @Override
            public PropertyLoader getPropertyLoader() {
                return new PropertyLoader();
            }
        };

        Object resolved = new LogFieldInjector()
                .resolve(field("databaseLogger"), "loggedBean", context);

        assertEquals("TestLib.database", ((Logger) resolved).getName());
    }

    @Test
    void testInvalidFieldTypeThrows() throws Exception {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new LogFieldInjector().resolve(field("plain"), "loggedBean", null));

        assertTrue(error.getMessage().contains("Logger"), error.getMessage());
    }

    @Test
    void testLoggerIsCachedPerName() {
        Logger first = EtherosLog.get("cache.test");
        Logger second = EtherosLog.get("cache.test");

        assertSame(first, second);
        assertEquals(1, createdNames.size());
    }

    private static Field field(String name) throws NoSuchFieldException {
        return LoggedBean.class.getDeclaredField(name);
    }

    private static final class TestLogger implements Logger {

        private final String name;

        private TestLogger(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void info(String message) {
        }

        @Override
        public void info(String message, Throwable throwable) {
        }

        @Override
        public void warn(String message) {
        }

        @Override
        public void warn(String message, Throwable throwable) {
        }

        @Override
        public void error(String message) {
        }

        @Override
        public void error(String message, Throwable throwable) {
        }

        @Override
        public void debug(String message) {
        }

        @Override
        public void debug(String message, Throwable throwable) {
        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }
    }
}
