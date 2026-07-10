/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */

package app.notesr.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;

public final class LoggingConfigurator {

  private static final String INFO_LEVEL_LOG_PATTERN =
      "%d{yyyy-MM-dd HH:mm:ss} %-5level %msg%n";

  private static final String TRACE_LEVEL_LOG_PATTERN =
      "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger: %msg%n";

  public static void configure(boolean trace) {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    context.reset();

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    String logPattern = trace ? TRACE_LEVEL_LOG_PATTERN : INFO_LEVEL_LOG_PATTERN;

    encoder.setContext(context);
    encoder.setPattern(logPattern);
    encoder.start();

    ConsoleAppender<ILoggingEvent> console = new ConsoleAppender<>();

    console.setContext(context);
    console.setEncoder(encoder);
    console.start();

    Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
    Level loggingLevel = trace ? Level.TRACE : Level.INFO;

    root.setLevel(loggingLevel);
    root.addAppender(console);
  }
}
