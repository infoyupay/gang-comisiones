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

import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.usecase.installkeys.InstallKeysView;
import com.yupay.gangcomisiones.usecase.setglobalconfig.SetGlobalConfigView;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

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

    public static @NotNull SetGlobalConfigView setGlobalConfigView(GlobalConfig userInput) {
        var view = mock(SetGlobalConfigView.class);
        doAnswer(inv -> {
            System.out.println(inv.getArgument(0, String.class));
            return null;
        }).when(view).showSuccess(anyString());
        doAnswer(inv -> {
            System.out.println(inv.getArgument(0, String.class));
            return null;
        }).when(view).showError(anyString());
        when(view.showSetGlobalConfigForm(any(GlobalConfig.class), anyBoolean()))
                .thenReturn(Optional.ofNullable(userInput));
        return view;
    }
}
