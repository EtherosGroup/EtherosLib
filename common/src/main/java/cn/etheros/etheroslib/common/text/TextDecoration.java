package cn.etheros.etheroslib.common.text;

/**
 * 文本装饰
 *
 * @author 西博卡兹
 * @since 2.2.0
 */
public enum TextDecoration {

    BOLD('l'),
    ITALIC('o'),
    UNDERLINED('n'),
    STRIKETHROUGH('m'),
    OBFUSCATED('k');

    private final char legacyCode;

    TextDecoration(char legacyCode) {
        this.legacyCode = legacyCode;
    }

    /** 对应的 § 代码 */
    public char legacyCode() {
        return legacyCode;
    }
}
