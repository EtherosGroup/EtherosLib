package cn.etheros.etheroslib.common.log.fixture;

import cn.etheros.etherosframework.di.annotation.Service;
import cn.etheros.etheroslib.common.log.InjectLogger;
import cn.etheros.etheroslib.common.log.Logger;

/**
 * 类级 {@link InjectLogger}：多个 Logger 字段，名字追加字段名。
 */
@Service
@InjectLogger
public class ClassLevelBean {

    private Logger logger;

    private Logger dbLogger;

    private String plain;

    public Logger getLogger() {
        return logger;
    }

    public Logger getDbLogger() {
        return dbLogger;
    }
}
