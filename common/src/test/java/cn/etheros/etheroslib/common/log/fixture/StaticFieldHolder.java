package cn.etheros.etheroslib.common.log.fixture;

import cn.etheros.etheroslib.common.log.InjectLogger;
import cn.etheros.etheroslib.common.log.Logger;

/**
 * 非法用法：static 字段不允许注入。
 */
public final class StaticFieldHolder {

    @InjectLogger
    private static Logger logger;

    private StaticFieldHolder() {
    }
}
