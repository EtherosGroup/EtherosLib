package cn.etheros.etheroslib.common.log.fixture;

import cn.etheros.etherosframework.di.annotation.Service;
import cn.etheros.etheroslib.common.log.InjectLogger;
import cn.etheros.etheroslib.common.log.Logger;

/**
 * 类级 {@link InjectLogger}：单个 Logger 字段，名字用类简名。
 */
@Service
@InjectLogger
public class SingleLoggerBean {

    private Logger logger;

    public Logger getLogger() {
        return logger;
    }
}
