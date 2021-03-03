package net.minecraftforge.mapsy.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.OutputStreamAppender;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Created by covers1624 on 11/1/21.
 */
public class LoggerCapture implements AutoCloseable {

    private final OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
    private final PatternLayoutEncoder encoder = new PatternLayoutEncoder();

    private final Set<Logger> loggers = Collections.newSetFromMap(new IdentityHashMap<>());

    public LoggerCapture(OutputStream out) {
        Context ctx = ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext();
        encoder.setPattern("[%d{HH:mm:ss.SSS}] [%thread/%level] [%logger{39}]: %msg%n");
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.setContext(ctx);
        encoder.start();

        appender.setName("logcapture");
        appender.setContext(ctx);
        appender.setEncoder(encoder);
        appender.setOutputStream(out);
        appender.start();
    }

    public void attachRoot() {
        addLogger(ContextSelectorStaticBinder.getSingleton()
                .getContextSelector()
                .getDefaultLoggerContext().getLogger(Logger.ROOT_LOGGER_NAME));
    }

    public void addLogger(Logger logger) {
        if (loggers.add(logger)) {
            logger.addAppender(appender);
        }
    }

    @Override
    public void close() {
        for (Logger logger : loggers) {
            logger.detachAppender(appender);
        }
        appender.stop();
        encoder.stop();
    }
}
