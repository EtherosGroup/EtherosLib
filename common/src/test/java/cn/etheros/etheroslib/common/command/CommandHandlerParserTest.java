package cn.etheros.etheroslib.common.command;

import cn.etheros.etheroslib.common.command.annotation.CommandHandler;
import cn.etheros.etheroslib.common.command.annotation.SubCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandHandlerParserTest {

    @Test
    void rejectsMissingCommandHandlerAnnotation() {
        assertThrows(IllegalArgumentException.class, () -> CommandHandlerParser.parse(new Object()));
    }

    @Test
    void rejectsSubCommandMethodWithoutSpecReturn() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new VoidMethodHandler()));
    }

    @Test
    void rejectsSubCommandMethodWithParameters() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new ParameterMethodHandler()));
    }

    @Test
    void rejectsMissingExecutor() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new NoExecutorHandler()));
    }

    @Test
    void rejectsNullSpec() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new NullSpecHandler()));
    }

    @Test
    void rejectsSpecFactoryCheckedException() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new CheckedFailureHandler()));
    }

    @Test
    void rejectsPrimitiveSenderType() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new PrimitiveSenderHandler()));
    }

    @Test
    void rejectsOptionalPrimitiveParameter() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new OptionalPrimitiveHandler()));
    }

    @Test
    void rejectsUnsupportedParameterType() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new UnsupportedTypeHandler()));
    }

    @Test
    void rejectsDuplicateParameterNames() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new DuplicateParameterHandler()));
    }

    @Test
    void rejectsDuplicateSubCommandPath() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new DuplicateHandler()));
    }

    @Test
    void rejectsConflictingAliases() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new ConflictAliasHandler()));
    }

    @Test
    void rejectsSubCommandAliasesWhenCommandHasAliases() {
        assertThrows(IllegalStateException.class,
                () -> CommandHandlerParser.parse(new AliasedCommandWithSubAlias()));
    }

    @Test
    void rejectsSubCommandPathAliasWhenCommandHasAliases() {
        assertThrows(IllegalStateException.class,
                () -> CommandHandlerParser.parse(new AliasedCommandWithPathAlias()));
    }

    @Test
    void rejectsRootExecutorAliases() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new RootWithAliasHandler()));
    }

    @CommandHandler(name = "void")
    public static final class VoidMethodHandler {

        @SubCommand("info")
        public void info() {
        }
    }

    @CommandHandler(name = "params")
    public static final class ParameterMethodHandler {

        @SubCommand("info")
        public SubCommandSpec info(String value) {
            return SubCommandSpec.of(context -> {
            });
        }
    }

    @CommandHandler(name = "noexec")
    public static final class NoExecutorHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder().build();
        }
    }

    @CommandHandler(name = "nullspec")
    public static final class NullSpecHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return null;
        }
    }

    @CommandHandler(name = "checked")
    public static final class CheckedFailureHandler {

        @SubCommand("info")
        public SubCommandSpec info() throws Exception {
            throw new Exception("boom");
        }
    }

    @CommandHandler(name = "primsender")
    public static final class PrimitiveSenderHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder()
                    .sender(int.class)
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "opprim")
    public static final class OptionalPrimitiveHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder()
                    .parameter(CommandParameter.optional("n", int.class))
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "unsupported")
    public static final class UnsupportedTypeHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder()
                    .parameter(CommandParameter.required("x", StringBuilder.class))
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "dupparam")
    public static final class DuplicateParameterHandler {

        @SubCommand("info")
        public SubCommandSpec info() {
            return SubCommandSpec.builder()
                    .parameter(CommandParameter.required("x", String.class))
                    .parameter(CommandParameter.required("x", String.class))
                    .execute(context -> {
                    })
                    .build();
        }
    }

    @CommandHandler(name = "dup")
    public static final class DuplicateHandler {

        @SubCommand("same")
        public SubCommandSpec first() {
            return SubCommandSpec.of(context -> {
            });
        }

        @SubCommand("same")
        public SubCommandSpec second() {
            return SubCommandSpec.of(context -> {
            });
        }
    }

    @CommandHandler(name = "conflict")
    public static final class ConflictAliasHandler {

        @SubCommand("one|shared")
        public SubCommandSpec first() {
            return SubCommandSpec.of(context -> {
            });
        }

        @SubCommand("two|shared")
        public SubCommandSpec second() {
            return SubCommandSpec.of(context -> {
            });
        }
    }

    @CommandHandler(name = "login", aliases = {"l"})
    public static final class AliasedCommandWithSubAlias {

        @SubCommand(value = "web", aliases = {"w"})
        public SubCommandSpec web() {
            return SubCommandSpec.of(context -> {
            });
        }
    }

    @CommandHandler(name = "login", aliases = {"l"})
    public static final class AliasedCommandWithPathAlias {

        @SubCommand("web|w")
        public SubCommandSpec web() {
            return SubCommandSpec.of(context -> {
            });
        }
    }

    @CommandHandler(name = "rootalias")
    public static final class RootWithAliasHandler {

        @SubCommand(value = "", aliases = {"r"})
        public SubCommandSpec root() {
            return SubCommandSpec.of(context -> {
            });
        }
    }
}
