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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default (uncaught) exception handler implementation that
 * logs unacught exceptions using SLF4J library.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class UncaughtExceptionLogger implements Thread.UncaughtExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UncaughtExceptionLogger.class);

    /**
     * Installs this handler as the default uncaught exception handler
     * for all threads in the JVM.
     */
    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.error("Uncaught exception in thread {}", t.getName(), e);
    }
}
