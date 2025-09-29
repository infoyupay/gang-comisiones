/*
 * gang-comisiones
 * COPYLEFT 2025
 * Ingenieria Informatica Yupay SACS
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 *  with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.yupay.gangcomisiones.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import com.yupay.gangcomisiones.LocalFiles;
import org.jetbrains.annotations.Contract;
import org.slf4j.LoggerFactory;

/**
 * Utility class to initialize logging component.
 * <br/>
 * This class configures Logback logging with:
 * <ul>
 *     <li>File logging with rotation in {@link LocalFiles#LOGS}</li>
 *     <li>Console output</li>
 *     <li>DEBUG logging level enforced</li>
 * </ul>
 * <br/>
 * Fallback using System.err and printStackTrace is enabled only if
 * logging configuration fails.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class LogConfig {
    /**
     * Private constructor to prevent instantiation.
     */
    @Contract(pure = true)
    private LogConfig() {
        // utility class
    }

    /**
     * Initializes logging component. This wires logging configuration, creating a file log
     * to {@link LocalFiles#LOGS}, and console output.
     * <br/>
     * It enforces the use of Logback library.
     * <br/>
     * It enforces DEBUG logging level.
     * <br/>
     * Suprreses warnings about using System.err and calling to printStackTrace, because
     * although it's not recommended in production environments, it's necessary as fallback
     * mechanism when logging cannot be properly initialized.
     */
    @SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace"})
    public static void initLogging() {
        try {
            var context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset(); // Clear default config

            var pattern = "%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] %logger{36} - %msg%n";

            // --- Encoders ---
            var fileEncoder = new PatternLayoutEncoder();
            fileEncoder.setContext(context);
            fileEncoder.setPattern(pattern);
            fileEncoder.start();

            var consoleEncoder = new PatternLayoutEncoder();
            consoleEncoder.setContext(context);
            consoleEncoder.setPattern(pattern);
            consoleEncoder.start();

            // --- Rolling file appender ---
            var rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
            rollingFileAppender.setContext(context);
            rollingFileAppender.setName("FILE");
            rollingFileAppender.setFile(LocalFiles.logs().resolve("gang-comisiones.log").toString());
            rollingFileAppender.setEncoder(fileEncoder);

            var rollingPolicy = new FixedWindowRollingPolicy();
            rollingPolicy.setContext(context);
            rollingPolicy.setParent(rollingFileAppender);
            rollingPolicy.setFileNamePattern(LocalFiles.logs().resolve("gang-comisiones.%i.log").toString());
            rollingPolicy.setMinIndex(1);
            rollingPolicy.setMaxIndex(5); // keep 5 old logs
            rollingPolicy.start();

            var triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
            triggeringPolicy.setContext(context);
            triggeringPolicy.setMaxFileSize(FileSize.valueOf("1MB")); // rotate at 1 MB
            triggeringPolicy.start();

            rollingFileAppender.setRollingPolicy(rollingPolicy);
            rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
            rollingFileAppender.start();

            // --- Console appender ---
            var consoleAppender = new ConsoleAppender<ILoggingEvent>();
            consoleAppender.setContext(context);
            consoleAppender.setName("CONSOLE");
            consoleAppender.setEncoder(consoleEncoder);
            consoleAppender.start();

            // --- Root logger ---
            var rootLogger = context.getLogger("ROOT");
            rootLogger.setLevel(Level.DEBUG);
            rootLogger.addAppender(rollingFileAppender);
            rootLogger.addAppender(consoleAppender);

        } catch (Exception e) {
            System.err.println("Failed to configure logging: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
