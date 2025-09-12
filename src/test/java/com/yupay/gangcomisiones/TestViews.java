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

import com.yupay.gangcomisiones.board.BoardView;
import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.bank.BankView;
import com.yupay.gangcomisiones.usecase.bank.manage.BankBoardView;
import com.yupay.gangcomisiones.usecase.commons.*;
import com.yupay.gangcomisiones.usecase.installkeys.InstallKeysView;
import com.yupay.gangcomisiones.usecase.setglobalconfig.SetGlobalConfigView;
import org.jetbrains.annotations.NotNull;
import org.mockito.stubbing.Stubber;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

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
    ///                                                                                                                                                                                                                              `showSetGlobalConfigForm` method.
    /// @return a mocked `SetGlobalConfigView` instance with the specified behavior.
    public static @NotNull SetGlobalConfigView setGlobalConfigView(GlobalConfig userInput) {
        var view = mock(SetGlobalConfigView.class);
        stubMessagePresenter(view);
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
    public static void stubMessagePresenter(MessagePresenter mockView) {
        mockShowMessageToSOut(MessageType.ERROR).when(mockView).showError(nullable(String.class));
        mockShowMessageToSOut(MessageType.INFORMATION).when(mockView).showSuccess(nullable(String.class));
        mockShowMessageToSOut(MessageType.WARNING).when(mockView).showWarning(nullable(String.class));
    }

    /**
     * Stubs the behavior of a {@link ListPresenter} mock instance to process a list of elements,
     * format them using the provided formatter function, and display the formatted elements alongside
     * a predefined emoji. If the provided list is empty, it outputs a message indicating the list is empty.
     * If a class cast issue occurs (e.g., invalid type in the list), an exception will be thrown.
     *
     * @param <T>       the type of elements in the list to be processed
     * @param mockView  the {@link ListPresenter} mock instance to be stubbed
     * @param formatter a {@link Function} that formats each element of the list into a string representation
     */
    @SuppressWarnings("unchecked")
    public static <T> void stubListPresenter(ListPresenter<T> mockView, Function<T, String> formatter) {
        doAnswer(inv -> {
            if (inv.getArgument(0) instanceof List<?> ls) {
                try {
                    if (ls.isEmpty()) {
                        System.out.println("EMPTY LIST!");
                    }
                    ((List<T>) ls).stream()
                            .map(formatter
                                    .andThen(Character.toString(0x1F499)::concat))
                            .forEach(System.out::println);
                } catch (ClassCastException e) {
                    throw new RuntimeException("Cannot cast items in mocked list presenter.", e);
                }
            }
            return null;
        }).when(mockView).showList((List<T>) nullable(List.class));
    }

    /**
     * Stubs the behavior of the {@link SecondaryView} mock instance to define
     * specific actions when its methods are invoked. This method configures
     * the mock to output predefined ASCII banners upon calling the `showView`
     * and `closeView` methods.
     *
     * @param mockView the mock instance of {@link SecondaryView} to stub with
     *                 predefined behaviors for its methods `showView` and `closeView`
     */
    public static void stubSecondaryView(SecondaryView mockView) {
        doAnswer(_ -> {
            System.out.println(AsciiBanners.WELCOME_USE_CASE_BANNER);
            return null;
        }).when(mockView).showView();
        doAnswer(_ -> {
            System.out.println(AsciiBanners.GOODBYE_USE_CASE_BANNER);
            return null;
        }).when(mockView).closeView();
    }

    /**
     * Configures the behavior of a mocked {@link BoardView} instance to stub its methods for presenting
     * lists, displaying messages, and propagating user privileges. This includes setting up behavior
     * for the following:
     * <ul>
     * <li>Presenting lists with formatted elements using a formatter function</li>
     * <li>Handling success, error, or warning messages</li>
     * <li>Propagating user privileges to the board's components</li>
     * </ul>
     *
     * @param <T>       the type of elements that can be displayed or processed by the {@link BoardView}
     * @param mockView  the {@link BoardView} mock instance to be configured with stubbed behaviors
     * @param formatter a {@link Function} to format each element of a list into a string representation
     */
    public static <T> void stubBoardView(BoardView<T> mockView, Function<T, String> formatter) {
        stubListPresenter(mockView, formatter);
        stubMessagePresenter(mockView);
        doAnswer(inv -> {
            if (inv.getArgument(0) instanceof User u) {
                System.out.printf(AsciiBanners.PRIVILEGE_PROPAGATION_ALERT, u.getRole());
            } else {
                System.err.println("Cannot propagate privileges of a null user in mock TestView!");
            }
            return null;
        }).when(mockView).propagatePrivileges(any(User.class));
    }

    /**
     * Creates and returns a mocked instance of the {@code BankView} interface.
     * The mocked instance is configured based on the provided {@link FormMode} and {@link Bank} result.
     * This allows customization of the user interaction behavior for handling {@link Bank} entities
     * in the specified form mode.
     *
     * @param mode   the {@link FormMode} that determines the interaction type:
     *               whether it is for creating, editing, or viewing a {@link Bank} entity.
     * @param result the {@link Bank} entity that is used in the mock configuration to simulate user interactions.
     * @return a mocked {@link BankView} instance configured for user interaction in the specified form mode.
     */
    public static @NotNull BankView bankView(FormMode mode, Bank result) {
        return bankView(mode, result, false);
    }

    /**
     * Creates and returns a mocked instance of the {@code BankView} interface with customized behavior
     * depending on the provided parameters. The returned mock instance is configured to handle different
     * user interactions based on the specified {@code FormMode}, the {@code Bank} result, and the
     * {@code anyMode} flag.
     *
     * @param mode     the {@link FormMode} that determines the context of the form: whether it is for
     *                 creating, editing, or viewing a {@link Bank} entity. If null, no user form
     *                 interaction is expected, except if {@code anyMode} is true.
     * @param result   the {@link Bank} instance that is returned by the mocked view when accepting
     *                 user input. May be {@code null}, in which case an empty {@code Optional}
     *                 will be returned.
     * @param anyMode  a boolean indicating whether the mock should allow any {@link FormMode} to be
     *                 used for user interaction when the {@code mode} is null.
     * @return a mocked {@code BankView} instance configured with the specified behavior for user
     *         interactions in the given form mode and result context.
     */
    public static @NotNull BankView bankView(FormMode mode, Bank result, boolean anyMode) {
        var view = mock(BankView.class);
        stubMessagePresenter(view);
        //If mode is null, no showUserForm interaction is expected.
        if (mode != null) {
            when(switch (mode) {
                case CREATE -> view.showUserForm(eq(mode));
                case EDIT -> view.showUserForm(any(Bank.class), eq(mode));
                default -> view.showUserForm(nullable(Bank.class), eq(mode));
            }).thenReturn(Optional.ofNullable(result));
        } else if (anyMode) {
            when(view.showUserForm(nullable(Bank.class), any(FormMode.class))).thenReturn(Optional.ofNullable(result));
        }
        return view;
    }

    /**
     * Creates and returns a mocked instance of the {@code BankBoardView} interface.
     * The returned mock is configured with predefined behaviors for presenting
     * {@code Bank} entities. Specifically, it uses a formatter to provide a
     * string representation of the {@code Bank} objects, with details including
     * the bank's ID, name, and active status.
     *
     * @return a mocked instance of {@code BankBoardView} with stubbed behaviors
     * for displaying {@code Bank} entities and integrating with secondary views.
     */
    public static BankBoardView bankBoardView() {
        var view = mock(BankBoardView.class);
        stubBoardView(view, bank ->
                "Bank : {id: %d; name: %s; active: %b}".formatted(bank.getId(), bank.getName(), bank.getActive())
        );
        stubSecondaryView(view);
        return view;
    }
}
