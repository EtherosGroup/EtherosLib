package cn.etheros.etheroslib.common.command;

import cn.etheros.etheroslib.common.command.annotation.Arg;
import cn.etheros.etheroslib.common.command.annotation.CommandHandler;
import cn.etheros.etheroslib.common.command.annotation.SubCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandResolverTest {

    private FakeCommandSender sender;
    private TestHandler handler;
    private CommandDefinition definition;

    @BeforeEach
    void setUp() {
        sender = new FakeCommandSender();
        sender.permissions.add("test.use");
        handler = new TestHandler();
        definition = CommandHandlerParser.parse(handler);
    }

    @Test
    void executesRootHandler() {
        assertTrue(definition.execute(sender, "test", new String[]{"hello"}));
        assertEquals(Collections.singletonList("root:hello"), handler.calls);
    }

    @Test
    void executesSubCommandAndAlias() {
        assertTrue(definition.execute(sender, "test", new String[]{"sub"}));
        assertTrue(definition.execute(sender, "test", new String[]{"s"}));
        assertEquals(Arrays.asList("sub", "sub"), handler.calls);
    }

    @Test
    void executesNestedSubCommand() {
        assertTrue(definition.execute(sender, "test", new String[]{"admin", "reload"}));
        assertEquals(Collections.singletonList("admin reload"), handler.calls);
    }

    @Test
    void usesUsageWhenArgumentMissing() {
        assertFalse(definition.execute(sender, "test", new String[]{"num"}));
        assertEquals(Collections.singletonList("用法：/test num <n>"), sender.messages);
    }

    @Test
    void rejectsInvalidArgument() {
        assertFalse(definition.execute(sender, "test", new String[]{"num", "abc"}));
        assertEquals(Collections.singletonList("错误：参数无效：n：不是整数"), sender.messages);
    }

    @Test
    void rejectsSubCommandWithoutPermission() {
        assertFalse(definition.execute(sender, "test", new String[]{"secret"}));
        assertEquals(Collections.singletonList("无权限：test.secret"), sender.messages);
    }

    @Test
    void rejectsWhenRootPermissionMissing() {
        FakeCommandSender anonymous = new FakeCommandSender();
        assertFalse(definition.execute(anonymous, "test", new String[]{"sub"}));
        assertEquals(Collections.singletonList("无权限：test.use"), anonymous.messages);
    }

    @Test
    void passesOptionalArgumentAsNull() {
        assertTrue(definition.execute(sender, "test", new String[]{"opt"}));
        assertEquals(Collections.singletonList("opt:null"), handler.calls);
    }

    @Test
    void injectsCommandContext() {
        assertTrue(definition.execute(sender, "test", new String[]{"ctx"}));
        assertEquals(Collections.singletonList("ctx:test"), handler.calls);
    }

    @Test
    void injectsCommandSenderAbstraction() {
        assertTrue(definition.execute(sender, "test", new String[]{"sender"}));
        assertEquals(Collections.singletonList("sender:fake"), handler.calls);
    }

    @Test
    void injectsPlatformRawSender() {
        sender.raw = new CustomSender();
        assertTrue(definition.execute(sender, "test", new String[]{"raw"}));
        assertEquals(Collections.singletonList("raw:CustomSender"), handler.calls);
    }

    @Test
    void rejectsSenderTypeMismatch() {
        assertFalse(definition.execute(sender, "test", new String[]{"rawonly"}));
        assertEquals(Collections.singletonList("命令执行者类型不匹配：CustomSender"), sender.messages);
    }

    @Test
    void suggestsSubCommandNames() {
        assertEquals(Collections.singletonList("sub"), definition.suggest(sender, "test", new String[]{"su"}));
    }

    @Test
    void hidesSubCommandWithoutPermission() {
        assertEquals(Collections.emptyList(), definition.suggest(sender, "test", new String[]{"sec"}));
        sender.permissions.add("test.secret");
        assertEquals(Collections.singletonList("secret"), definition.suggest(sender, "test", new String[]{"sec"}));
    }

    @Test
    void suggestsByCustomCompleter() {
        assertEquals(Collections.singletonList("fast"), definition.suggest(sender, "test", new String[]{"mode", "f"}));
    }

    @Test
    void returnsEmptyForUnknownPath() {
        assertEquals(Collections.emptyList(), definition.suggest(sender, "test", new String[]{"nope", "x"}));
    }

    @Test
    void rejectsDuplicateSubCommandPath() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new DuplicateHandler()));
    }

    @Test
    void executesAliasesOnEveryPathSegment() {
        assertTrue(definition.execute(sender, "test", new String[]{"adm", "rl"}));
        assertTrue(definition.execute(sender, "test", new String[]{"adm", "reload"}));
        assertTrue(definition.execute(sender, "test", new String[]{"admin", "rl"}));
        assertEquals(Arrays.asList("admin reload", "admin reload", "admin reload"), handler.calls);
    }

    @Test
    void suggestsCanonicalNameInsteadOfAlias() {
        assertEquals(Collections.singletonList("admin"), definition.suggest(sender, "test", new String[]{"ad"}));
    }

    @Test
    void rejectsConflictingAliases() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new ConflictAliasHandler()));
    }

    @Test
    void allowsSubCommandsWhenCommandHasAliases() {
        AliasedCommandHandler aliasedHandler = new AliasedCommandHandler();
        CommandDefinition aliased = CommandHandlerParser.parse(aliasedHandler);
        assertTrue(aliased.execute(sender, "l", new String[]{"web"}));
        assertEquals(Collections.singletonList("web"), aliasedHandler.calls);
    }

    @Test
    void rejectsSubCommandAliasesWhenCommandHasAliases() {
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new AliasedCommandWithSubAlias()));
        assertThrows(IllegalStateException.class, () -> CommandHandlerParser.parse(new AliasedCommandWithPathAlias()));
    }

    public static final class CustomSender {
    }

    public static final class ModeTabProvider implements TabProvider {

        @Override
        public Collection<String> suggest(TabContext context) {
            return Arrays.asList("fast", "slow");
        }
    }

    @CommandHandler(name = "test", permission = "test.use")
    public static final class TestHandler {

        final List<String> calls = new ArrayList<String>();

        @SubCommand("")
        void root(CommandSender sender, @Arg("word") String word) {
            calls.add("root:" + word);
        }

        @SubCommand(value = "sub", aliases = {"s"})
        void sub(CommandSender sender) {
            calls.add("sub");
        }

        @SubCommand(value = "secret", permission = "test.secret")
        void secret(CommandSender sender) {
            calls.add("secret");
        }

        @SubCommand("num")
        void num(CommandSender sender, @Arg("n") int value) {
            calls.add("num:" + value);
        }

        @SubCommand("mode")
        void mode(CommandSender sender,
                  @Arg(value = "m", completer = ModeTabProvider.class) String mode) {
            calls.add("mode:" + mode);
        }

        @SubCommand("sender")
        void sender(CommandSender sender) {
            calls.add("sender:" + sender.name());
        }

        @SubCommand("raw")
        void raw(CustomSender sender) {
            calls.add("raw:" + sender.getClass().getSimpleName());
        }

        @SubCommand("rawonly")
        void rawOnly(CustomSender sender) {
            calls.add("rawonly");
        }

        @SubCommand("opt")
        void opt(CommandSender sender, @Arg(value = "x", optional = true) String value) {
            calls.add("opt:" + value);
        }

        @SubCommand("ctx")
        void ctx(CommandContext context) {
            calls.add("ctx:" + context.label());
        }

        @SubCommand("admin|adm reload|rl")
        void adminReload(CommandSender sender) {
            calls.add("admin reload");
        }
    }

    @CommandHandler(name = "dup")
    public static final class DuplicateHandler {

        @SubCommand("same")
        void first(CommandSender sender) {
        }

        @SubCommand("same")
        void second(CommandSender sender) {
        }
    }

    @CommandHandler(name = "conflict")
    public static final class ConflictAliasHandler {

        @SubCommand("one|shared")
        void first(CommandSender sender) {
        }

        @SubCommand("two|shared")
        void second(CommandSender sender) {
        }
    }

    @CommandHandler(name = "login", aliases = {"l"})
    public static final class AliasedCommandHandler {

        final List<String> calls = new ArrayList<String>();

        @SubCommand("web")
        void web(CommandSender sender) {
            calls.add("web");
        }
    }

    @CommandHandler(name = "login", aliases = {"l"})
    public static final class AliasedCommandWithSubAlias {

        @SubCommand(value = "web", aliases = {"w"})
        void web(CommandSender sender) {
        }
    }

    @CommandHandler(name = "login", aliases = {"l"})
    public static final class AliasedCommandWithPathAlias {

        @SubCommand("web|w")
        void web(CommandSender sender) {
        }
    }
}
