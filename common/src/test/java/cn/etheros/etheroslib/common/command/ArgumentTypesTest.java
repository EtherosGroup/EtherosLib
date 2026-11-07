package cn.etheros.etheroslib.common.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgumentTypesTest {

    private enum Mode {
        FAST, SLOW
    }

    @Test
    void parsesStringAsIs() throws CommandArgumentException {
        assertEquals("abc", ArgumentTypes.resolve(String.class).parse("abc"));
    }

    @Test
    void parsesInteger() throws CommandArgumentException {
        assertEquals(42, ArgumentTypes.resolve(int.class).parse("42"));
    }

    @Test
    void rejectsInvalidInteger() {
        ArgumentType<?> type = ArgumentTypes.resolve(Integer.class);
        assertThrows(CommandArgumentException.class, () -> type.parse("abc"));
    }

    @Test
    void parsesBooleanIgnoringCase() throws CommandArgumentException {
        ArgumentType<?> type = ArgumentTypes.resolve(boolean.class);
        assertEquals(Boolean.TRUE, type.parse("TRUE"));
        assertEquals(Boolean.FALSE, type.parse("false"));
        assertThrows(CommandArgumentException.class, () -> type.parse("yes"));
    }

    @Test
    void resolvesEnumDynamically() throws CommandArgumentException {
        ArgumentType<?> type = ArgumentTypes.resolve(Mode.class);
        assertNotNull(type);
        assertEquals(Mode.SLOW, type.parse("slow"));
        assertThrows(CommandArgumentException.class, () -> type.parse("nope"));
        assertEquals(2, type.suggest(null).size());
    }

    @Test
    void returnsNullForUnsupportedType() {
        assertNull(ArgumentTypes.resolve(StringBuilder.class));
    }
}
