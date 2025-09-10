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

package com.yupay.gangcomisiones.usecase.registry;

import com.yupay.gangcomisiones.AppContext;

import java.util.function.Supplier;

public abstract class UseCaseSupplier<U> implements Supplier<U> {
    public abstract ViewRegistry viewRegistry();
    public  Supplier<UseCaseControllerRegistry> controllerRegistry(){
        return ControllerRegistries::defaultRegistry;
    }
    public  Supplier<AppContext>  appContextSupplier(){
        return AppContext::getInstance;
    }
}
