package cn.etheros.etheroslib.common.log.fixture;

import cn.etheros.etherosframework.di.annotation.PostConstruct;
import cn.etheros.etherosframework.di.annotation.Service;
import cn.etheros.etheroslib.common.log.Log;
import cn.etheros.etheroslib.common.log.Logger;

/**
 * 测试用 Bean：验证 {@link Log} 字段注入与 @PostConstruct 时序。
 */
@Service
public class LoggedBean {

    @Log
    private Logger logger;

    @Log("database")
    private Logger databaseLogger;

    private String plain;

    private String loggerNameInPostConstruct;

    @PostConstruct
    void init() {
        this.loggerNameInPostConstruct = logger == null ? null : logger.getName();
    }

    public Logger getLogger() {
        return logger;
    }

    public Logger getDatabaseLogger() {
        return databaseLogger;
    }

    public String getLoggerNameInPostConstruct() {
        return loggerNameInPostConstruct;
    }
}
