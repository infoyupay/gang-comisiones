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
/**
 * Provides a general-purpose, type-based registry abstraction decoupled from any domain-specific package.
 * This package exposes the contracts and default implementation so they can be reused across modules.
 * <br/>
 * <strong>Contents:</strong>
 * <ul>
 *   <li>{@link com.yupay.gangcomisiones.registry.TypeRegistry}: contract for registering and resolving components by {@link Class}.</li>
 *   <li>{@link com.yupay.gangcomisiones.registry.DefaultTypeRegistry}: default thread-safe implementation.</li>
 * </ul>
 * <br/>
 * <strong>Responsibilities:</strong>
 * <ul>
 *   <li>Associate component types with suppliers via {@link com.yupay.gangcomisiones.registry.TypeRegistry#register(Class, java.util.function.Supplier)}.</li>
 *   <li>Resolve instances on demand with {@link com.yupay.gangcomisiones.registry.TypeRegistry#resolve(Class)}.</li>
 *   <li>Manage lifecycle of registrations with {@link com.yupay.gangcomisiones.registry.TypeRegistry#unregister(Class)} and
 *       {@link com.yupay.gangcomisiones.registry.TypeRegistry#isRegistered(Class)}.</li>
 *   <li>Facilitate testing through {@link com.yupay.gangcomisiones.registry.TypeRegistry#registerInstance(Class, Object)}.</li>
 * </ul>
 * <br/>
 * <strong>Design highlights:</strong>
 * <ul>
 *   <li>Resolution errors surface as {@link com.yupay.gangcomisiones.exceptions.TypeRegistryException}.</li>
 *   <li>The default implementation uses a concurrent map to support multi-threaded access.</li>
 *   <li>Suppliers can return new instances or singletons, depending on their implementation.</li>
 *   <li>Domain-specific aliases (e.g., a view registry) can extend {@link com.yupay.gangcomisiones.registry.TypeRegistry} without adding methods,
 *       preserving semantic clarity while reusing the core behavior.</li>
 * </ul>
 * <br/>
 * <strong>Typical usage:</strong>
 * <ol>
 *   <li>Create or obtain a {@link com.yupay.gangcomisiones.registry.DefaultTypeRegistry} instance.</li>
 *   <li>Register suppliers for component types with
 *       {@link com.yupay.gangcomisiones.registry.TypeRegistry#register(Class, java.util.function.Supplier)}.</li>
 *   <li>Resolve components where needed via {@link com.yupay.gangcomisiones.registry.TypeRegistry#resolve(Class)}.</li>
 *   <li>Optionally unregister components using {@link com.yupay.gangcomisiones.registry.TypeRegistry#unregister(Class)}.</li>
 * </ol>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
package com.yupay.gangcomisiones.registry;