package cn.etheros.etheroslib.common.log;

import cn.etheros.etherosframework.di.core.ApplicationContext;
import cn.etheros.etherosframework.di.core.PropertyLoader;
import cn.etheros.etherosframework.di.spi.FieldInjectionProviders;
import cn.etheros.etheroslib.common.log.fixture.Anchor;
import cn.etheros.etheroslib.common.log.fixture.ClassLevelBean;
import cn.etheros.etheroslib.common.log.fixture.LoggedBean;
import cn.etheros.etheroslib.common.log.fixture.SingleLoggerBean;
import cn.etheros.etheroslib.common.log.fixture.StaticFieldHolder;
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
 * {@link InjectLogger} 注入（字段级 / 类级）与 {@code {}} 占位符格式化。
 */
class LogInjectionTest {

    private final List<String> createdNames = new ArrayList<String>();
    private final List<String> messages = new ArrayList<String>();
    private final List<Throwable> throwables = new ArrayList<Throwable>();

    @BeforeEach
    void setUp() {
        EtherosLog.reset();
        FieldInjectionProviders.clear();
        createdNames.clear();
        messages.clear();
        throwables.clear();
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

    // ---------- 字段级 ----------

    @Test
    void testSupportsFieldLevelAnnotation() throws Exception {
        LogFieldInjector injector = new LogFieldInjector();

        assertTrue(injector.supports(field(LoggedBean.class, "logger")));
        assertTrue(injector.supports(field(LoggedBean.class, "databaseLogger")));
        assertFalse(injector.supports(field(LoggedBean.class, "plain")));
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

        Object resolved = new LogFieldInjector().resolve(field(LoggedBean.class, "logger"), "loggedBean", emptyContext);

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
                .resolve(field(LoggedBean.class, "databaseLogger"), "loggedBean", context);

        assertEquals("TestLib.database", ((Logger) resolved).getName());
    }

    @Test
    void testInvalidFieldTypeThrows() throws Exception {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new LogFieldInjector().resolve(field(LoggedBean.class, "plain"), "loggedBean", null));

        assertTrue(error.getMessage().contains("Logger"), error.getMessage());
    }

    @Test
    void testStaticFieldRejected() throws Exception {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new LogFieldInjector()
                        .resolve(field(StaticFieldHolder.class, "logger"), "holder", null));

        assertTrue(error.getMessage().contains("static"), error.getMessage());
    }

    @Test
    void testLoggerIsCachedPerName() {
        Logger first = EtherosLog.get("cache.test");
        Logger second = EtherosLog.get("cache.test");

        assertSame(first, second);
        assertEquals(1, createdNames.size());
    }

    // ---------- 类级 ----------

    @Test
    void testClassLevelInjectsAllLoggerFields() {
        ApplicationContext context = ApplicationContext.run(Anchor.class);
        try {
            ClassLevelBean bean = context.getBean(ClassLevelBean.class);
            assertNotNull(bean);
            assertEquals("TestLib.ClassLevelBean.logger", bean.getLogger().getName());
            assertEquals("TestLib.ClassLevelBean.dbLogger", bean.getDbLogger().getName());

            SingleLoggerBean single = context.getBean(SingleLoggerBean.class);
            assertNotNull(single);
            assertEquals("TestLib.SingleLoggerBean", single.getLogger().getName());
        } finally {
            context.shutdown();
        }
    }

    @Test
    void testClassLevelOnlyClaimsLoggerFields() throws Exception {
        LogFieldInjector injector = new LogFieldInjector();

        assertTrue(injector.supports(field(ClassLevelBean.class, "logger")));
        assertTrue(injector.supports(field(ClassLevelBean.class, "dbLogger")));
        assertFalse(injector.supports(field(ClassLevelBean.class, "plain")),
                "类级注解不应认领非 Logger 字段");
    }

    // ---------- {} 占位符 ----------

    @Test
    void testVarargsFormatting() {
        Logger logger = EtherosLog.get("fmt.test");

        logger.warn("玩家 {} 在 {} 登录失败", "Steve", "12:00");

        assertEquals("玩家 Steve 在 12:00 登录失败", lastMessage());
    }

    @Test
    void testExtraArgumentsAppended() {
        Logger logger = EtherosLog.get("fmt.extra");

        logger.warn("a {} b", 1, 2);

        assertEquals("a 1 b 2", lastMessage());
    }

    @Test
    void testMissingArgumentsKeepPlaceholder() {
        Logger logger = EtherosLog.get("fmt.missing");

        logger.info("{} 和 {}", "only");

        assertEquals("only 和 {}", lastMessage());
    }

    @Test
    void testTrailingThrowableBecomesException() {
        Logger logger = EtherosLog.get("fmt.throwable");
        RuntimeException boom = new RuntimeException("boom");

        logger.error("失败 {}", "x", boom);

        assertEquals("失败 x", lastMessage());
        assertSame(boom, lastThrowable());
    }

    @Test
    void testFixedArityThrowableStillWorks() {
        Logger logger = EtherosLog.get("fmt.fixed");
        IllegalStateException boom = new IllegalStateException("boom");

        logger.error("加载失败", boom);

        assertEquals("加载失败", lastMessage());
        assertSame(boom, lastThrowable());
    }

    @Test
    void testDebugSkippedWhenDisabled() {
        Logger logger = EtherosLog.get("fmt.debug");

        logger.debug("x {}", 1);

        assertTrue(messages.isEmpty(), "debug 关闭时不应产生日志");
    }

    private String lastMessage() {
        assertFalse(messages.isEmpty(), "没有捕获到日志");
        return messages.get(messages.size() - 1);
    }

    private Throwable lastThrowable() {
        assertFalse(throwables.isEmpty(), "没有捕获到异常");
        return throwables.get(throwables.size() - 1);
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        return type.getDeclaredField(name);
    }

    private final class TestLogger implements Logger {

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
            messages.add(message);
        }

        @Override
        public void info(String message, Throwable throwable) {
            messages.add(message);
            throwables.add(throwable);
        }

        @Override
        public void warn(String message) {
            messages.add(message);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            messages.add(message);
            throwables.add(throwable);
        }

        @Override
        public void error(String message) {
            messages.add(message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            messages.add(message);
            throwables.add(throwable);
        }

        @Override
        public void debug(String message) {
            messages.add(message);
        }

        @Override
        public void debug(String message, Throwable throwable) {
            messages.add(message);
            throwables.add(throwable);
        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }
    }
}
