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

package com.yupay.gangcomisiones.assertions;

import com.yupay.gangcomisiones.AppContext;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.AbstractLongAssert;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Represents a utility class for asserting the existence of audit logs related
 * to specific entities in the system. This class provides methods to validate
 * audit log presence based on entity identifiers and ensures that the data meets
 * the required conditions.
 * <br/>
 * The following key operations can be performed by this class:
 * <ul>
 *     <li>Validate that audit logs exist for a given entity based on its identifier.</li>
 *     <li>Provide fluent assertion capabilities for further validations on results.</li>
 *     <li>Seamlessly integrate with JPA using an {@link EntityManager} for database queries.</li>
 * </ul>
 *
 * @param ctx the app context from where to create an entity manager.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record AuditLogAssertion(AppContext ctx) {

    /**
     * Creates a new instance of {@link AuditLogAssertion} with the provided application context.
     * <br/>
     * This utility method initializes an assertion object used to validate audit log entries
     * within a given application context.
     * <br/>
     * <ul>
     *     <li>Ensures the application context is not null.</li>
     *     <li>Returns a new {@code AuditLogAssertion} instance associated with the provided context.</li>
     * </ul>
     * <br/>
     * <b>Note:</b> Clients of this method should ensure the proper context is passed to facilitate accurate log assertions.
     *
     * @param ctx The {@link AppContext} representing the application's context. Must not be null.
     *            This context defines the boundaries within which audit log queries are executed.
     * <br/>
     * @return A new {@link AuditLogAssertion} instance initialized with the provided application context.
     */
    @Contract("_ -> new")
    public static @NotNull AuditLogAssertion withContext(AppContext ctx) {
        return new AuditLogAssertion(ctx);
    }

    /**
     * Asserts that at least one audit log exists for a given entity. This method validates
     * that the provided entity is not null, its corresponding identifier is not null, and
     * queries the database to check for audit logs associated with the entity's identifier.
     * <br/>
     * Utilizes a {@link Function} to extract the entity's identifier and internally invokes
     * a helper method to assert the existence of the audit logs in the database.
     * <br/>
     * <ul>
     *     <li>Ensures that the entity provided is not null.</li>
     *     <li>Ensures that the entity's identifier is not null.</li>
     *     <li>Queries the database to locate logs for the given identifier.</li>
     * </ul>
     *
     * @param <T>    The type of the entity being checked for associated audit logs.
     *               <br/>
     * @param entity The entity for which audit logs are to be verified.
     *               Must not be null.
     *               <br/>
     * @param id     A {@link Function} to extract the identifier from the entity.
     *               The extracted identifier must not be null.
     *               <br/>
     * @return An {@link AbstractLongAssert} object to facilitate further
     * assertions regarding the result.
     */
    public <T> AbstractLongAssert<?> assertHasLog(T entity, @NotNull Function<T, ? extends Number> id) {
        assertThat(entity).as("Entity to look for audit log should not be null").isNotNull();
        var entityId = id.apply(entity);
        assertThat(entityId).as("Entity id to look for audit log should not be null").isNotNull();
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            return assertLogExists(em, entityId);
        }
    }

    /**
     * Verifies that at least one log exists in the database for a specified entity ID.
     * The method executes a query to count the logs associated with the given entity ID
     * and asserts that the count is greater than or equal to 1.
     *
     * @param em The {@link EntityManager} instance used to execute the database query.
     *           Must not be null.
     *           <br/>
     * @param id The identifier of the entity for which the logs are being checked.
     *           Must not be null.
     *           <br/>
     * @return An {@link AbstractLongAssert} object to allow method chaining for further assertions.
     */
    public AbstractLongAssert<?> assertLogExists(@NotNull EntityManager em, Number id) {
        var r = em.createQuery("SELECT COUNT(L) FROM  AuditLog L WHERE L.entityId =:id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return assertThat(r)
                .as("Expected at least 1 AuditLog for entity id = %s", id)
                .isGreaterThanOrEqualTo(1L);
    }
}
