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

import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.usecase.bank.BankView;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.commons.MessageType;
import com.yupay.gangcomisiones.usecase.commons.UserPrompter;
import com.yupay.gangcomisiones.usecase.installkeys.InstallKeysView;
import com.yupay.gangcomisiones.usecase.setglobalconfig.SetGlobalConfigView;
import org.jetbrains.annotations.NotNull;
import org.mockito.stubbing.Stubber;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Provides utility methods for creating mocked views used in testing.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class TestViews {
    /**
     * Creates and returns a mocked instance of the {@code InstallKeysView} interface.
     * The returned mock instance is configured to return the specified path
     * when the {@code showOpenDialogForZip()} method is called.
     *
     * @param userInput the path to be returned by the mock when {@code showOpenDialogForZip()} is invoked
     * @return a mocked instance of {@code InstallKeysView} with the specified behavior for {@code showOpenDialogForZip()}
     */
    public static @NotNull InstallKeysView installKeysView(Path userInput) {
        var r = mock(InstallKeysView.class);
        when(r.showOpenDialogForZip()).thenReturn(userInput);
        return r;
    }

    /// Creates and returns a mocked instance of the `SetGlobalConfigView` interface.
    /// The mocked instance is configured with pre-defined behaviors for certain methods:
    /// - `showSuccess(String message)`: Prints the provided message to the standard output.
    /// - `showError(String message)`: Prints the provided error message to the standard output.
    /// - `showSetGlobalConfigForm(GlobalConfig config, boolean bootstrapMode)`: Returns the provided `userInput`
    ///   encapsulated in an `Optional`, or an empty `Optional` if `userInput` is `null`.
    ///
    /// @param userInput the `GlobalConfig` instance to be returned by the
    ///                                                                                                                                         `showSetGlobalConfigForm` method.
    /// @return a mocked `SetGlobalConfigView` instance with the specified behavior.
    public static @NotNull SetGlobalConfigView setGlobalConfigView(GlobalConfig userInput) {
        var view = mock(SetGlobalConfigView.class);
        stubPrompter(view);
        when(view.showSetGlobalConfigForm(any(GlobalConfig.class), anyBoolean()))
                .thenReturn(Optional.ofNullable(userInput));
        return view;
    }

    /**
     * Configures and returns a {@link Stubber} that redirects a message to the standard output stream
     * with an associated emoji based on the specified message type.
     * <br/>
     * The method sets up a mock behavior such that when invoked, it outputs a formatted string to
     * {@code System.out} with an emoji representing the message type followed by the message content.
     * <br/>
     * The emoji equivalence is:
     * <ul>
     *     <li>{@link MessageType#ERROR} -&gt; &#x1F6AB;</li>
     *     <li>{@link MessageType#WARNING} -&gt; &#x26A0;</li>
     *     <li>{@link MessageType#INFORMATION} -&gt; &#x1F4AC;</li>
     *     <li>{@code null} -&gt; &#x1F4A9;</li>
     * </ul>
     *
     * @param requiredType the {@link MessageType} determining the type of message to be displayed
     *                     (e.g., error, warning, information). This determines the emoji used in the output.
     * @return a {@link Stubber} configured for mocking system message displays to standard output.
     */
    public static Stubber mockShowMessageToSOut(MessageType requiredType) {
        return doAnswer(inv -> {
            var msg = Objects.toString(inv.getArgument(0));
            var emoji = switch (requiredType) {
                case ERROR -> Character.toString(0x1F6AB);
                case WARNING -> "⚠";
                case INFORMATION -> Character.toString(0x1F4AC);
                case null -> Character.toString(0x1F4A9);
            };
            System.out.printf("%s => %s%n", emoji, msg);
            return null;
        });
    }

    /**
     * Configures a mock instance of {@link UserPrompter} to stub predefined behaviors for displaying different
     * types of system messages (error, warning, success).
     *
     * @param mockView the mock instance of {@code UserPrompter} whose message display methods are being stubbed to
     *                 produce corresponding output messages to standard output (System.out) for predefined message types.
     */
    public static void stubPrompter(UserPrompter<?> mockView) {
        mockShowMessageToSOut(MessageType.ERROR).when(mockView).showError(nullable(String.class));
        mockShowMessageToSOut(MessageType.INFORMATION).when(mockView).showSuccess(nullable(String.class));
        mockShowMessageToSOut(MessageType.WARNING).when(mockView).showWarning(nullable(String.class));
    }

    /// Creates and returns a mocked instance of the `BankView` interface.
    /// The returned mock instance is configured with predefined behaviors:
    /// - When the `showMessage` method is called, it mocks displaying the message.
    /// - When the `showUserForm` method is invoked, it returns an [Optional] containing
    ///   the provided [Bank] result or an empty [Optional] based on the input.
    ///
    /// @param mode   the [FormMode] indicating the mode in which the user form will be displayed.
    ///               If this value is null, no request to show user form should be made.
    /// @param result the [Bank] object to be returned within an [Optional]
    ///               when the user form is shown
    /// @return a mocked instance of `BankView` with the specified behaviors
    public static @NotNull BankView bankView(FormMode mode, Bank result) {
        var view = mock(BankView.class);
        stubPrompter(view);
        //If mode is null, no showUserForm interaction is expected.
        if (mode != null) {
            when(switch (mode) {
                case CREATE -> view.showUserForm(eq(mode));
                case EDIT -> view.showUserForm(eq(result), eq(mode));
                default -> view.showUserForm(nullable(Bank.class), eq(mode));
            }).thenReturn(Optional.ofNullable(result));
        }
        return view;
    }
}
