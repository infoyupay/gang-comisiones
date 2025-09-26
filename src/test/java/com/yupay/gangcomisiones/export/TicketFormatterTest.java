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

package com.yupay.gangcomisiones.export;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.TextFormats;
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.Transaction;
import org.assertj.core.api.AbstractAssert;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Test class for validating the functionality of the {@link TicketFormatter} class, specifically its ability
 * to format transaction tickets correctly when integrating with persisted domain entities
 * and respecting formatting constraints.
 * <br/>
 * This test class extends {@link AbstractPostgreIntegrationTest}, enabling interaction with a real
 * PostgreSQL database for integration testing.
 * <br/>
 * The test methods in this class aim to ensure:
 * <ul>
 *   <li>Domain entities involved in the ticketing process can be persisted and retrieved successfully.</li>
 *   <li>The {@code TicketFormatter} correctly integrates data from the {@link Transaction} and
 *       {@link GlobalConfig} entities.</li>
 *   <li>The formatted ticket adheres to the expected structure, contents, and constraints.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class TicketFormatterTest extends AbstractPostgreIntegrationTest {
    /**
     * Cleans up persisted entities in the database before each test method is executed.
     * <br/>
     * This method ensures that the test environment is reset to a consistent state by removing
     * any persisted data that might interfere with subsequent tests.
     * <br/>
     * <ol>
     *   <li>Invokes the static {@code clean()} method from the {@code TestPersistedEntities} helper class.</li>
     *   <li>Passes the {@code EntityManagerFactory} from the test context to remove any persisted entities.</li>
     * </ol>
     * <br/>
     * Use this setup method with the {@code @BeforeEach} annotation to ensure that it is executed
     * before each test in the class.
     */
    @BeforeEach
    void clean() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Tests the {@link TicketFormatter#format(GlobalConfig, Transaction)} method to ensure it produces a correctly
     * formatted ticket string based on the provided transaction and global configuration.
     * <br/>
     * This method performs the following:
     * <br/>
     * <ol>
     *   <li>Creates and persists required sample entities using {@link TestPersistedEntities}:
     *       <ul>
     *           <li>A {@link Transaction} representing a specific financial operation.</li>
     *           <li>A {@link GlobalConfig} containing global configuration settings.</li>
     *       </ul>
     *   </li>
     *   <li>Sets the current user in the session to match the cashier of the persisted {@code Transaction}.</li>
     *   <li>Invokes {@link TicketFormatter#format(GlobalConfig, Transaction)} to generate the ticket string based on the
     *       persisted entities.</li>
     *   <li>Prints each line of the ticket string in a formatted style for debugging.</li>
     *   <li>Uses {@link TicketAssert} to perform validations on the formatted ticket, ensuring:
     *       <ul>
     *           <li>It includes the correct company name from the {@code GlobalConfig}.</li>
     *           <li>It includes the concept name from the {@code Transaction}.</li>
     *           <li>The amount and commission are correctly formatted as currency values.</li>
     *           <li>The user information matches the cashier's username.</li>
     *           <li>The ticket adheres to a maximum width of 32 characters.</li>
     *       </ul>
     *   </li>
     * </ol>
     * <br/>
     * This test ensures that the {@code TicketFormatter#format} implementation correctly integrates and formats
     * data from multiple domain entities while respecting formatting constraints.
     */
    @Test
    void testFormatter() {

        record Pulled(Transaction tx, GlobalConfig cfg) {
        }
        var samples = performInTransaction(em -> {
            var tx = TestPersistedEntities.persistTransaction(em);
            var cfg = TestPersistedEntities.persistGlobalConfig(em);
            return new Pulled(tx, cfg);
        });
        ctx.getUserSession().setCurrentUser(samples.tx().getCashier());

        var str = TicketFormatter.format(samples.cfg(), samples.tx());
        str.lines().map("|%-32s|"::formatted).forEach(System.out::println);

        TicketAssert.assertThatTicket(str)
                .hasCompanyName(samples.cfg.getLegalName())
                .hasConcept(samples.tx.getConceptName())
                .hasAmount(TextFormats.getCurrencyFormat().format(samples.tx.getAmount()))
                .hasCommission(TextFormats.getCurrencyFormat().format(samples.tx.getCommission()))
                .hasUser(samples.tx.getCashier().getUsername())
                .hasMaxWidth(32);
    }
}

/**
 * Custom assertion to verify ticket outputs.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class TicketAssert extends AbstractAssert<TicketAssert, String> {

    private final List<String> lines;

    /**
     * Constructs a new {@code TicketAssert} instance.
     * <br/>
     * This private constructor initializes the assertion object for validating properties of a ticket,
     * processing its lines for structured checks.
     * <br/>
     *
     * @param actual the ticket content as a {@code String} to be asserted against.
     */
    private TicketAssert(String actual) {
        super(actual, TicketAssert.class);
        this.lines = actual.lines().toList();
    }

    /**
     * Asserts the properties of a ticket represented as a {@code String}.
     * <br/>
     * This factory method creates an instance of {@link TicketAssert}, allowing verification of expected
     * ticket attributes such as company name, concept, amount, commission, user, and formatting constraints.
     * <br/>
     * The method supports chainable assertions to validate various aspects of the ticket in a fluent style.
     *
     * @param actual the ticket content as a {@code String} to be validated.
     *                             <ul>
     *                               <li>It must contain the textual representation of a ticket.</li>
     *                               <li>The content should be formatted correctly to match expected assertions.</li>
     *                             </ul>
     *               <br/>
     * @return an instance of {@link TicketAssert}, initialized with the provided {@code actual} ticket content
     * to perform subsequent assertions.
     */
    @Contract("_ -> new")
    public static @NotNull TicketAssert assertThatTicket(String actual) {
        return new TicketAssert(actual);
    }

    /**
     * Verifies that the ticket contains the specified company name.
     * <br/>
     * This method checks if the ticket includes the {@code expected} company name by inspecting its lines.
     * If the company name is not found, an assertion error is thrown with a detailed message.
     * <br/>
     * <ul>
     *   <li>Ensures the ticket text is not null before performing the assertion.</li>
     *   <li>Triggers a failure if the ticket does not include the company name.</li>
     *   <li>Supports fluent API chaining after this assertion.</li>
     * </ul>
     * <br/>
     *
     * @param expected the expected company name to be present in the ticket.
     *                                 <ul>
     *                                   <li>This value should be non-null and represent the expected legal or display name of the company.</li>
     *                                   <li>The check is case-sensitive and validates based on exact match.</li>
     *                                 </ul>
     *                 <br/>
     * @return the current {@link TicketAssert} instance for further assertions.
     */
    public TicketAssert hasCompanyName(String expected) {
        isNotNull();
        if (lines.stream().noneMatch(l -> l.contains(expected))) {
            failWithMessage("Expected ticket to contain company name <%s> but was <%s>", expected, actual);
        }
        return this;
    }

    /**
     * Verifies that the ticket contains the specified concept.
     * <br/>
     * This method checks whether the provided {@code expected} concept is present in the ticket by searching for
     * the phrase "Concepto: " followed by the expected value in the ticket lines. If the concept is not found,
     * an assertion error is raised with a detailed failure message.
     * <br/>
     * <ul>
     *   <li>Ensures the ticket text is not null before performing the assertion.</li>
     *   <li>Triggers a failure if the ticket does not include the specified concept.</li>
     *   <li>Supports fluent API chaining for additional assertions.</li>
     * </ul>
     * <br/>
     *
     * @param expected the expected concept name to be present in the ticket.
     *                                 <ul>
     *                                   <li>This value should be non-null and represent the expected concept as part of the ticket.</li>
     *                                   <li>The match is based on an exact value check, including case sensitivity.</li>
     *                                 </ul>
     *                 <br/>
     * @return the current {@link TicketAssert} instance for further assertions.
     */
    public TicketAssert hasConcept(String expected) {
        isNotNull();
        if (lines.stream().noneMatch(l -> l.contains("Concepto: " + expected))) {
            failWithMessage("Expected ticket to contain concept <%s> but was <%s>", expected, actual);
        }
        return this;
    }

    /**
     * Verifies that the ticket contains the specified amount.
     * <br/>
     * This method asserts that the provided {@code expected} amount is present in the ticket by
     * searching through its lines. If the specified amount is not found, an assertion error is
     * raised, detailing the expected and actual ticket content.
     * <br/>
     * <ul>
     *   <li>Ensures the ticket text is not null before performing the assertion.</li>
     *   <li>Triggers a failure if the ticket does not include the amount.</li>
     *   <li>Supports fluent API chaining for additional assertions.</li>
     * </ul>
     * <br/>
     *
     * @param expected the expected amount to be present in the ticket.
     *                                 <ul>
     *                                   <li>This value should be a non-null string, formatted as the expected amount.</li>
     *                                   <li>The match considers character-level precision, including whitespace handling.</li>
     *                                 </ul>
     *                 <br/>
     * @return the current {@link TicketAssert} instance for further assertions.
     */
    public TicketAssert hasAmount(String expected) {
        isNotNull();
        if (lines.stream().noneMatch(l -> l.matches(".*" + expected.replace(" ", "\\s*") + "$"))) {
            failWithMessage("Expected ticket to contain monto <%s> but was <%s>", expected, actual);
        }
        return this;
    }

    /**
     * Verifies that the ticket contains the specified commission value.
     * <br/>
     * This method ensures that the provided {@code expected} commission amount appears in the ticket by
     * searching through its lines. If the expected commission is not present, an assertion error is
     * thrown with a descriptive failure message.
     * <br/>
     * <ul>
     *   <li>Validates that the ticket content is not null before performing the assertion.</li>
     *   <li>Throws an assertion failure if the specified commission is not included.</li>
     *   <li>Supports fluent API chaining for additional ticket assertions.</li>
     * </ul>
     * <br/>
     *
     * @param expected the expected commission value to be present in the ticket.
     *                                 <ul>
     *                                   <li>This value should be a non-null string representing the commission.</li>
     *                                   <li>The match checks for an exact string value, including case and spacing considerations.</li>
     *                                 </ul>
     *                 <br/>
     * @return the current {@link TicketAssert} instance for further chained assertions.
     */
    public TicketAssert hasCommission(String expected) {
        isNotNull();
        if (lines.stream().noneMatch(l -> l.matches(".*" + expected.replace(" ", "\\s*") + "$"))) {
            failWithMessage("Expected ticket to contain comisión <%s> but was <%s>", expected, actual);
        }
        return this;
    }

    /**
     * Verifies that the ticket contains the specified user.
     * <br/>
     * This method checks whether the provided {@code expected} user is present in the ticket
     * by searching for the phrase "Usuario: " followed by the expected value in the ticket's lines.
     * If the user is not found, an assertion error is raised with a detailed failure message.
     * <br/>
     * <ul>
     *   <li>Ensures the ticket content is not null before performing the assertion.</li>
     *   <li>Triggers an assertion failure if the specified user is not included in the ticket.</li>
     *   <li>Supports fluent API chaining for further assertions.</li>
     * </ul>
     * <br/>
     *
     * @param expected the expected user to be present in the ticket.
     *                                 <ul>
     *                                   <li>This value should be non-null and represent the exact user name to search within the ticket.</li>
     *                                   <li>Matches are case-sensitive and depend on a precise match of the value.</li>
     *                                 </ul>
     *                 <br/>
     * @return the current {@link TicketAssert} instance for further assertions.
     */
    public TicketAssert hasUser(String expected) {
        isNotNull();
        if (lines.stream().noneMatch(l -> l.contains("Usuario: " + expected))) {
            failWithMessage("Expected ticket to contain user <%s> but was <%s>", expected, actual);
        }
        return this;
    }

    /**
     * Verifies that all lines in the ticket have a maximum width specified by the given value.
     * <br/>
     * This method checks whether the length of each line in the ticket does not exceed the specified width.
     * If any line exceeds the maximum width, an assertion error is raised with a detailed failure message.
     * <br/>
     * <ul>
     *   <li>Ensures the ticket text is not null before performing the assertion.</li>
     *   <li>Validates that all lines adhere to the specified maximum width constraint.</li>
     *   <li>Supports fluent API chaining for additional assertions.</li>
     * </ul>
     * <br/>
     *
     * @param width the maximum line width allowed for the ticket content.
     *                           <ul>
     *                             <li>This value should be a positive integer representing the maximum number of characters in any line.</li>
     *                             <li>Lines exceeding this width will result in a failure with a descriptive error message.</li>
     *                           </ul>
     *              <br/>
     */
    public void hasMaxWidth(int width) {
        isNotNull();
        var allOk = lines.stream().allMatch(l -> l.length() <= width);
        if (!allOk) {
            failWithMessage("Expected all lines to have max width <%d> but some were different: %s", width, lines);
        }
    }
}
