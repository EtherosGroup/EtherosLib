package cn.etheros.etheroslib.common.command;

import cn.etheros.etheroslib.common.command.annotation.CommandHandler;
import cn.etheros.etheroslib.common.command.annotation.SubCommand;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandErrorTest {

    private final FakeCommandSender sender = new FakeCommandSender();

    private void execute(CommandDefinition definition, String... args) {
        definition.execute(sender, "test", args);
    }

    @Test
    void playerOnlyUsesDefaultMessage() {
        CommandDefinition definition = CommandHandlerParser.parse(new PlayerOnlyHandler());
        execute(definition, "info");
        assertEquals(Collections.singletonList("该命令只能由玩家执行"), sender.messages);
    }

    @Test
    void playerOnlyUsesCustomMessage() {
        CommandDefinition definition = CommandHandlerParser.parse(new PlayerOnlyCustomHandler());
        execute(definition, "info");
        assertEquals(Collections.singletonList("请以玩家身份执行"), sender.messages);
    }

    @Test
    void playerOnlyExecutesForPlayer() {
        sender.raw = new Player();
        CommandDefinition definition = CommandHandlerParser.parse(new PlayerOnlyHandler());
        assertTrue(definition.execute(sender, "test", new String[]{"info"}));
        assertEquals(Collections.emptyList(), sender.messages);
    }

    @Test
    void noPermissionUsesCustomMessage() {
        CommandDefinition definition = CommandHandlerParser.parse(new PermissionHandler());
        execute(definition, "locked");
        assertEquals(Collections.singletonList("缺少权限：secret.use"), sender.messages);
    }

    @Test
    void noPermissionFallsBackToClassHandler() {
        CommandDefinition definition = CommandHandlerParser.parse(new ClassHandler());
        execute(definition, "locked");
        assertEquals(Collections.singletonList("class:NO_PERMISSION"), sender.messages);
    }

    @Test
    void classHandlerWinsOverGlobalHandler() {
        CommandDefinition definition = CommandHandlerParser.parse(new ClassHandler(),
                context -> "global:" + context.error());
        execute(definition, "locked");
        assertEquals(Collections.singletonList("class:NO_PERMISSION"), sender.messages);
    }

    @Test
    void classHandlerNullFallsThroughToGlobalHandler() {
        CommandDefinition definition = CommandHandlerParser.parse(new NullClassHandler(),
                context -> "global:" + context.error());
        execute(definition, "locked");
        assertEquals(Collections.singletonList("global:NO_PERMISSION"), sender.messages);
    }

    @Test
    void globalHandlerAppliedWhenNothingElseMatches() {
        CommandDefinition definition = CommandHandlerParser.parse(new GlobalTargetHandler(),
                context -> "global:" + context.error());
        execute(definition, "locked");
        assertEquals(Collections.singletonList("global:NO_PERMISSION"), sender.messages);
    }

    @Test
    void builtInDefaultAppliedWhenNoHandlerMatches() {
        CommandDefinition definition = CommandHandlerParser.parse(new GlobalTargetHandler());
        execute(definition, "locked");
        assertEquals(Collections.singletonList("无权限：secret.use"), sender.messages);
    }

    @Test
    void specCatchAllHandlerAppliesWhenNoExactHandler() {
        CommandDefinition definition = CommandHandlerParser.parse(new CatchAllHandler());
        execute(definition, "locked");
        assertEquals(Collections.singletonList("fallback:NO_PERMISSION"), sender.messages);
    }

    @Test
    void specExactHandlerWinsOverCatchAllHandler() {
        CommandDefinition definition = CommandHandlerParser.parse(new ExactHandler());
        execute(definition, "locked");
        assertEquals(Collections.singletonList("exact"), sender.messages);
    }

    @Test
    void emptyMessageSuppressesOutput() {
        CommandDefinition definition = CommandHandlerParser.parse(new SilentHandler());
        assertFalse(definition.execute(sender, "test", new String[]{"info"}));
        assertEquals(Collections.emptyList(), sender.messages);
    }

    @Test
    void invalidArgumentExposesContext() {
        CommandDefinition definition = CommandHandlerParser.parse(new InspectHandler());
        execute(definition, "num", "abc");
        assertEquals(Collections.singletonList("amount|abc|不是整数"), sender.messages);
    }

    @Test
    void invalidArgumentCountExposesContext() {
        CommandDefinition definition = CommandHandlerParser.parse(new InspectHandler());
        execute(definition, "range");
        assertEquals(Collections.singletonList("1-2|用法：/test range <a> [b]"), sender.messages);
    }

    @Test
    void noExecutorExposesPathAndUsage() {
        CommandDefinition definition = CommandHandlerParser.parse(new GroupHandler());
        execute(definition, "admin");
        assertEquals(Collections.singletonList("path=[admin];usage=用法：/test admin <子命令>"), sender.messages);
    }

    @Test
    void executionErrorRethrowsByDefault() {
        CommandDefinition definition = CommandHandlerParser.parse(new BoomHandler());
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> definition.execute(sender, "test", new String[]{"fail"}));
        assertEquals("boom", exception.getMessage());
    }

    @Test
    void checkedExecutionErrorRethrowsAsIllegalState() {
        CommandDefinition definition = CommandHandlerParser.parse(new BoomHandler());
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> definition.execute(sender, "test", new String[]{"checked"}));
        assertEquals("checked", exception.getCause().getMessage());
    }

    @Test
    void executionErrorUsesCustomHandler() {
        CommandDefinition definition = CommandHandlerParser.parse(new BoomHandler());
        assertFalse(definition.execute(sender, "test", new String[]{"custom"}));
        assertEquals(Collections.singletonList("handled:boom"), sender.messages);
    }

    @Test
    void executionErrorUsesClassHandler() {
        CommandDefinition definition = CommandHandlerParser.parse(new ClassExecutionErrorHandler());
        assertFalse(definition.execute(sender, "test", new String[]{"fail"}));
        assertEquals(Collections.singletonList("class:EXECUTION_ERROR"), sender.messages);
    }

    @Test
    void handlerExceptionFallsBackToDefaultMessage() {
        CommandDefinition definition = CommandHandlerParser.parse(new BrokenHandler());
        execute(definition, "info");
        assertEquals(Collections.singletonList("该命令只能由玩家执行"), sender.messages);
    }

    @Test
    void i18nStyleParameterNameTranslation() {
        CommandDefinition chinese = CommandHandlerParser.parse(new I18nHandler(false));
        chinese.execute(sender, "test", new String[]{"count", "abc"});
        assertEquals(Collections.singletonList("参数无效：数量：不是整数"), sender.messages);

        FakeCommandSender englishSender = new FakeCommandSender();
        CommandDefinition english = CommandHandlerParser.parse(new I18nHandler(true));
        english.execute(englishSender, "test", new String[]{"count", "abc"});
        assertEquals(Collections.singletonList("Invalid argument: amount: not an integer"), englishSender.messages);
    }

    @Test
    void unknownArgumentNameIsRejected() {
        CommandDefinition definition = CommandHandlerParser.parse(new UnknownArgumentHandler());
        assertThrows(IllegalArgumentException.class,
                () -> definition.execute(sender, "test", new String[]{"info", "x"}));
    }

    @Test
    void typedArgumentRejectsMismatch() {
        CommandDefinition definition = CommandHandlerParser.parse(new TypedArgumentHandler());
        assertThrows(IllegalArgumentException.class,
                () -> definition.execute(sender, "test", new String[]{"num", "42"}));
    }

    @Test
    void optionalArgumentIsNullWhenMissing() {
        CommandDefinition definition = CommandHandlerParser.parse(new TypedArgumentHandler());
        assertTrue(definition.execute(sender, "test", new String[]{"opt"}));
    }

    public static final class Player {
    }

    @CommandHandler(name = "who")
    public static final class PlayerOnlyHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder()
                    .sender(Player.class)
                    .execute(context -> context.sender(Player.class))
                    .build();
        }
    }

    @CommandHandler(name = "who")
    public static final class PlayerOnlyCustomHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder()
                    .sender(Player.class)
                    .onError(CommandError.PLAYER_ONLY, "请以玩家身份执行")
                    .execute(context -> context.sender(Player.class))
                    .build();
        }
    }

    @CommandHandler(name = "perm")
    public static final class PermissionHandler {

        @SubCommand(value = "locked", permission = "secret.use")
        public SubCommandSpec locked() {
            return SubCommandSpec.builder()
                    .onError(CommandError.NO_PERMISSION, context -> "缺少权限：" + context.permission())
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "class")
    public static final class ClassHandler implements CommandErrorHandler {

        @SubCommand(value = "locked", permission = "secret.use")
        public SubCommandSpec locked() {
            return SubCommandSpec.of(context -> {
            });
        }

        @Override
        public String message(CommandErrorContext context) {
            return "class:" + context.error();
        }
    }

    @CommandHandler(name = "nullclass")
    public static final class NullClassHandler implements CommandErrorHandler {

        @SubCommand(value = "locked", permission = "secret.use")
        public SubCommandSpec locked() {
            return SubCommandSpec.of(context -> {
            });
        }

        @Override
        public String message(CommandErrorContext context) {
            return null;
        }
    }

    @CommandHandler(name = "global")
    public static final class GlobalTargetHandler {

        @SubCommand(value = "locked", permission = "secret.use")
        public SubCommandSpec locked() {
            return SubCommandSpec.of(context -> {
            });
        }
    }

    @CommandHandler(name = "catchall")
    public static final class CatchAllHandler {

        @SubCommand(value = "locked", permission = "secret.use")
        public SubCommandSpec locked() {
            return SubCommandSpec.builder()
                    .onError(context -> "fallback:" + context.error())
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "exact")
    public static final class ExactHandler {

        @SubCommand(value = "locked", permission = "secret.use")
        public SubCommandSpec locked() {
            return SubCommandSpec.builder()
                    .onError(CommandError.NO_PERMISSION, "exact")
                    .onError(context -> "fallback")
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "silent")
    public static final class SilentHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder()
                    .sender(Player.class)
                    .onError(CommandError.PLAYER_ONLY, "")
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "inspect")
    public static final class InspectHandler {

        @SubCommand("num")
        public SubCommandSpec num() {
            return SubCommandSpec.builder()
                    .parameter(CommandParameter.required("amount", Integer.class))
                    .onError(CommandError.INVALID_ARGUMENT,
                            context -> context.argumentName() + "|" + context.argumentInput()
                                    + "|" + context.detail())
                    .execute(context -> {
                    })
                    .build();
        }

        @SubCommand("range")
        public SubCommandSpec range() {
            return SubCommandSpec.builder()
                    .parameter(CommandParameter.required("a", String.class))
                    .parameter(CommandParameter.optional("b", String.class))
                    .onError(CommandError.INVALID_ARGUMENT_COUNT,
                            context -> context.minArguments() + "-" + context.maxArguments()
                                    + "|" + context.usage())
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "group")
    public static final class GroupHandler implements CommandErrorHandler {

        @SubCommand("admin reload")
        public SubCommandSpec reload() {
            return SubCommandSpec.of(context -> {
            });
        }

        @Override
        public String message(CommandErrorContext context) {
            return "path=" + context.path() + ";usage=" + context.usage();
        }
    }

    @CommandHandler(name = "boom")
    public static final class BoomHandler {

        @SubCommand("fail")
        public SubCommandSpec fail() {
            return SubCommandSpec.of(context -> {
                throw new IllegalStateException("boom");
            });
        }

        @SubCommand("custom")
        public SubCommandSpec custom() {
            return SubCommandSpec.builder()
                    .onError(CommandError.EXECUTION_ERROR,
                            context -> "handled:" + context.exception().getMessage())
                    .execute(context -> {
                        throw new IllegalStateException("boom");
                    })
                    .build();
        }

        @SubCommand("checked")
        public SubCommandSpec checked() {
            return SubCommandSpec.of(context -> {
                throw new Exception("checked");
            });
        }
    }

    @CommandHandler(name = "classboom")
    public static final class ClassExecutionErrorHandler implements CommandErrorHandler {

        @SubCommand("fail")
        public SubCommandSpec fail() {
            return SubCommandSpec.of(context -> {
                throw new IllegalStateException("boom");
            });
        }

        @Override
        public String message(CommandErrorContext context) {
            return "class:" + context.error();
        }
    }

    @CommandHandler(name = "broken")
    public static final class BrokenHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder()
                    .sender(Player.class)
                    .onError(CommandError.PLAYER_ONLY, context -> {
                        throw new RuntimeException("handler boom");
                    })
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "i18n")
    public static final class I18nHandler implements CommandErrorHandler {

        private final boolean english;

        I18nHandler(boolean english) {
            this.english = english;
        }

        @SubCommand("count")
        public SubCommandSpec count() {
            return SubCommandSpec.builder()
                    .parameter(CommandParameter.required("amount", Integer.class))
                    .execute(context -> context.argument("amount"))
                    .build();
        }

        @Override
        public String message(CommandErrorContext context) {
            if (context.error() != CommandError.INVALID_ARGUMENT) {
                return null;
            }
            String name = "amount".equals(context.argumentName())
                    ? (english ? "amount" : "数量") : context.argumentName();
            String detail = english && "不是整数".equals(context.detail())
                    ? "not an integer" : context.detail();
            return english
                    ? "Invalid argument: " + name + ": " + detail
                    : "参数无效：" + name + "：" + detail;
        }
    }

    @CommandHandler(name = "unknown")
    public static final class UnknownArgumentHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder()
                    .parameter(CommandParameter.required("value", String.class))
                    .execute(context -> context.argument("nope"))
                    .build();
        }
    }

    @CommandHandler(name = "typed")
    public static final class TypedArgumentHandler {

        @SubCommand("num")
        public SubCommandSpec num() {
            return SubCommandSpec.builder()
                    .parameter(CommandParameter.required("n", Integer.class))
                    .execute(context -> context.argument("n", String.class))
                    .build();
        }

        @SubCommand("opt")
        public SubCommandSpec opt() {
            return SubCommandSpec.builder()
                    .parameter(CommandParameter.optional("x", String.class))
                    .execute(context -> {
                        assertNull(context.argument("x"));
                    })
                    .build();
        }
    }
}
