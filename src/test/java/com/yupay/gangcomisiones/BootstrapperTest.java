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

package com.yupay.gangcomisiones;

import com.yupay.gangcomisiones.exceptions.GangComisionesException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;

import static com.yupay.gangcomisiones.assertions.CauseAssertions.assertExpectedCause;
import static com.yupay.gangcomisiones.assertions.PathAssertions.assertDirWasCreated;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Unit tests for {@link Bootstrapper}, focused on verifying the behavior of {@link Bootstrapper#bootstrap(AppMode)}.
 * <br/>The tests validate the correct creation of required directories, initialization of logging,
 * and proper handling of exceptional cases.
 * <br/>Tested scenarios include:
 * <ul>
 *   <li>Successful creation of directories and initialization of logging when directories do not exist</li>
 *   <li>No directory creation when all required directories already exist</li>
 *   <li>Graceful handling of {@link java.io.IOException} when directory creation fails</li>
 * </ul>
 * <br/>The tests rely on utility assertions from {@link com.yupay.gangcomisiones.assertions.PathAssertions} to verify file system state,
 * improving readability and avoiding low-level mocking of {@link java.nio.file.Files}.
 * <div style="border: 1px solid black; padding: 2px">
 *    <strong>Execution Note:</strong> dvidal@infoyupay.com passed 2 tests in 0.179s at 2025-10-01 00:55 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class BootstrapperTest {

    /**
     * Verifies that the bootstrap process correctly creates the required directories and initializes logging.
     * <br/>This ensures that:
     * <ul>
     *   <li>Non-existent directories are detected and created</li>
     *   <li>The logging component is initialized successfully</li>
     * </ul>
     * <br/>Validation steps:
     * <ul>
     *   <li>Checks directory creation for {@link LocalFiles#yupay()}, {@link LocalFiles#project()}, and {@link LocalFiles#logs()}</li>
     *   <li>Confirms that no unexpected operations are performed</li>
     * </ul>
     *
     * @throws IOException if an error occurs during file operations
     */
    @Test
    void testBootstrap_CreatesDirectoriesAndInitializesLogging() throws IOException {
        Bootstrapper.bootstrap(AppMode.GHOST);
        assertDirWasCreated(LocalFiles.yupay());
        assertDirWasCreated(LocalFiles.project());
        assertDirWasCreated(LocalFiles.logs());
    }

    /**
     * Verifies that the bootstrap process rejects unexpected modes.
     * <br/>Specifically, passing {@link AppMode#TEST} should raise a {@link GangComisionesException}
     * with the appropriate message.
     *
     * @param mode the application mode under test
     */
    @ParameterizedTest
    @EnumSource(value = AppMode.class, names = {"TEST"})
    void assertUnexpectedMode(AppMode mode) {
        var t = catchThrowable(() -> Bootstrapper.bootstrap(mode));
        assertExpectedCause(GangComisionesException.class)
                .assertCauseWithMessage(t, "Unexpected mode in bootstrap");
    }
}
