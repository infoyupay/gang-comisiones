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

package com.yupay.gangcomisiones.usecase;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * A generic implementation of the {@code Answer} interface that prints the details of
 * arguments passed to the mocked method during invocation. Each argument is printed
 * along with its index, type, and value.
 * <br/>
 * This class is mainly used for debugging or testing purposes where the arguments
 * to a method need to be logged for inspection. The method invocation does not
 * return any value and always returns {@code null}.
 *
 * @param <T> the return type of the answer, which is ignored in this implementation
 * @author InfoYupay SACS
 * @version 1.0
 */
public class PrintLineAnswer<T> implements Answer<T> {
    /**
     * Creates and returns a new instance of {@code PrintLineAnswer}.
     *
     * @param <T> the type of the answer
     * @return a new instance of {@code PrintLineAnswer}
     */
    @Contract(value = " -> new", pure = true)
    public static <T> @NotNull PrintLineAnswer<T> get() {
        return new PrintLineAnswer<>();
    }

    @Override
    public T answer(@NotNull InvocationOnMock invocation) {
        var args = invocation.getArguments();
        for (int i = 0; i < args.length; i++) {
            System.out.printf("args[%d] (%s): %s%n", i,
                    args[i] == null
                            ? "null"
                            : args[i].getClass().getSimpleName(),
                    args[i]);
        }
        return null;
    }
}
