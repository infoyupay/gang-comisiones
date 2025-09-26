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

package com.yupay.gangcomisiones.model;


import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Integration tests for validating the functionality and persistence of the {@code AuditLog} entity.
 * <br/>
 * This class extends {@code AbstractPostgreIntegrationTest} to leverage the PostgreSQL testing environment.
 * <br/>
 * Key use cases include:
 * <ul>
 *   <li>Validating persistence of {@code AuditLog} entities with user information.</li>
 *   <li>Ensuring database constraints on {@code AuditLog} fields are enforced during insert operations.</li>
 * </ul>
 *
 * Features tested:
 * <ol>
 *   <li>Correct persistence of {@code AuditLog} entities with non-null user associations.</li>
 *   <li>Validation failure when attempting to insert an {@code AuditLog} entity with a {@code null} user.</li>
 *   <li>Validation failure when attempting to insert an {@code AuditLog} entity with an empty {@code action} field.</li>
 * </ol>
 *
 * Test scenarios:
 * <ul>
 *   <li>
 *     <b>{@code shouldPersistAuditLogWithUser}</b><br/>
 *     Verifies that an {@code AuditLog} is correctly persisted with associated user information and other mandatory fields.
 *   </li>
 *   <li>
 *     <b>{@code shouldFailWhenUserIsNull}</b><br/>
 *     Ensures that persisting an {@code AuditLog} without an associated user violates database constraints and fails.
 *   </li>
 *   <li>
 *     <b>{@code shouldFailWhenActionIsEmpty}</b><br/>
 *     Validates that persisting an {@code AuditLog} with an empty {@code action} field violates the database's non-empty check constraint.
 *   </li>
 * </ul>
 * <div>
 *     <strong>Execution note: </strong> tested by dvidal@infoyupay.com 3 passed in 2.246ms.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class AuditLogIntegrationTest extends AbstractPostgreIntegrationTest {
    /**
     * Truncates tables before each execution.
     */
    @BeforeEach
    void cleanTable() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Tests that the audit log is persisted with the user information.
     */
    @Test
    void shouldPersistAuditLogWithUser() {
        //Arrange
        //1. define a data carrier.
        record UserAndLog(User user, AuditLog log) {
        }
        //Create a user and log
        var userAndLog = TestPersistedEntities.performInTransaction(ctx, em -> {
            var user = TestPersistedEntities.persistRootUser(em);
            var log = AuditLog.builder()
                    .user(user)
                    .action("LOGIN")
                    .entity("User")
                    .entityId(user.getId())
                    .details("User logged in")
                    .computerName("workstation-01")
                    .build();
            //ACT
            em.persist(log);
            em.flush();
            em.refresh(log);
            return new UserAndLog(user, log);
        });

        // Assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(userAndLog.log.getId()).isNotNull();
            softly.assertThat(userAndLog.log.getEventStamp()).isNotNull();
            softly.assertThat(userAndLog.log.getAction()).isEqualTo("LOGIN");
            softly.assertThat(userAndLog.log.getUser().getId()).isEqualTo(userAndLog.user.getId());
        });
    }

    /**
     * Tests that AuditLog insert fails when user is null.
     */
    @Test
    void shouldFailWhenUserIsNull() {
        //Arrange
        //1. Invalid AuditLog
        var log = AuditLog.builder()
                .action("DELETE")
                .computerName("pc01")
                .build();
        //Act
        performAndExpectFlushFailure(
                SQLException.class,
                em -> em.persist(log));
        //Assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(log.getEventStamp()).isNull();
            softly.assertThat(log.getUser()).isNull();
        });
    }

    /**
     * Tests that AuditLog insert fails when action is empty.
     */
    @Test
    void shouldFailWhenActionIsEmpty() {
        var logHolder = new AtomicReference<AuditLog>();
        performAndExpectFlushFailure(SQLException.class,
                "chk_audit_action_nonempty",
                em -> {
                    var user = TestPersistedEntities.persistRootUser(em);
                    var log = AuditLog.builder()
                            .user(user)
                            .action("") // violates CHECK constraint
                            .computerName("pc02")
                            .build();
                    logHolder.set(log);
                    em.persist(log);
                });
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(logHolder.get()).isNotNull();
            softly.assertThat(logHolder.get().getEventStamp()).isNull();
            softly.assertThat(logHolder.get().getAction()).isBlank();
        });
    }

}
