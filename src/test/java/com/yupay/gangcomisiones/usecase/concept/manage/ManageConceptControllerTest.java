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

package com.yupay.gangcomisiones.usecase.concept.manage;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.TestViews;
import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.model.ConceptType;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.concept.ConceptView;
import com.yupay.gangcomisiones.usecase.registry.ControllerRegistries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the functionality and behavior of {@link ManageConceptController}.
 * <br/>
 * This class verifies the orchestration of the "Manage Concept" feature, ensuring the controller wires
 * callbacks, opens and closes the view, propagates privileges and delegates to sub-use cases for creating,
 * editing and listing concepts. It mirrors the structure and assertions of the
 * {@link com.yupay.gangcomisiones.usecase.bank.manage.ManageBankController} suite.
 * <div style="border: 1px solid black; padding: 2px;">
 * <strong>Execution note:</strong> Tested by: dvidal@infoyupay.com, passed 1 tests in 4.416s at 2025-09-13 20:52 UTC-5
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class ManageConceptControllerTest extends AbstractPostgreIntegrationTest {
    final AtomicReference<Concept> conceptRef = new AtomicReference<>();
    User user;
    ConceptView conceptView;
    ConceptBoardView board;
    ManageConceptController controller;

    /**
     * Arranges the test environment before each test execution.
     * <ul>
     *   <li>Truncates tables and resets cached entities.</li>
     *   <li>Persists an ADMIN user and assigns it to the session.</li>
     *   <li>Prepares a new {@link Concept} instance for create/edit flows.</li>
     *   <li>Creates mocked {@link ConceptBoardView} and {@link ConceptView} and registers them.</li>
     *   <li>Resolves the {@link ManageConceptController} from the default registry.</li>
     * </ul>
     */
    @BeforeEach
    void arrange() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        user = performInTransaction(ctx, TestPersistedEntities::persistAdminUser);
        conceptRef.set(Concept.builder()
                .name("Internet")
                .type(ConceptType.RATE)
                .value(new BigDecimal("0.1000"))
                .active(true)
                .build());

        // Board and form mocks
        board = mock(ConceptBoardView.class);
        TestViews.stubBoardView(board, c ->
                "Concept : {id: %d; name: %s; type: %s; value: %s; active: %b}".formatted(
                        c.getId(), c.getName(), c.getType(), c.getValue(), c.getActive()));
        TestViews.stubSecondaryView(board);

        conceptView = mock(ConceptView.class);
        TestViews.stubMessagePresenter(conceptView);
        when(conceptView.showUserForm(eq(FormMode.CREATE))).thenAnswer(_ -> Optional.ofNullable(conceptRef.get()));
        when(conceptView.showUserForm(any(Concept.class), eq(FormMode.EDIT))).thenAnswer(_ -> Optional.ofNullable(conceptRef.get()));

        when(board.getConceptView()).thenReturn(conceptView);

        viewRegistry.registerInstance(ConceptBoardView.class, board);
        viewRegistry.registerInstance(ConceptView.class, conceptView);
        ctx.getUserSession().setCurrentUser(user);

        controller = ControllerRegistries.defaultRegistry().resolve(ManageConceptController.class);
    }

    /**
     * Cleans up the test environment after each test execution.
     * <ul>
     *   <li>Unregisters mocked views and resets references.</li>
     *   <li>Clears the current user from the session.</li>
     * </ul>
     */
    @AfterEach
    void cleanUp() {
        user = null;
        conceptRef.set(null);
        viewRegistry.unregister(ConceptBoardView.class);
        viewRegistry.unregister(ConceptView.class);
        conceptView = null;
        board = null;
        controller = null;
        ctx.getUserSession().setCurrentUser(null);
    }

    /**
     * Exercises the end-to-end orchestration when an ADMIN user opens the Manage Concept board and performs
     * create, edit and list actions. It verifies callbacks wiring, privilege propagation, view lifecycle and
     * user feedback messages.
     */
    @Test
    void givenLoggedAdmin_whenIntentsAllOptions_success() {
        /* ACT */
        controller.startUseCase();
        controller.createConcept();
        pause();
        // Load concept to edit from DB
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            em.createQuery("SELECT C FROM Concept C", Concept.class)
                    .getResultList()
                    .stream().findAny().ifPresent(conceptRef::set);
        }
        controller.editConcept(conceptRef.get());
        pause();
        controller.listConcept();
        pause();
        board.closeView();

        /* ASSERT */
        verify(board, atLeastOnce()).showView();
        verify(board, atLeastOnce()).showList(anyList());
        verify(board).closeView();
        verify(board).insert(any(Concept.class));
        verify(board).replace(any(Concept.class));
        verify(board).propagatePrivileges(any(User.class));

        verify(conceptView).showUserForm(eq(FormMode.CREATE));
        verify(conceptView).showUserForm(nullable(Concept.class), eq(FormMode.EDIT));
        var order = inOrder(conceptView);
        order.verify(conceptView).showSuccess(contains("creado exitosamente"));
        order.verify(conceptView).showSuccess(contains("actualizado exitosamente"));
    }

    /**
     * Pauses the execution of the current thread for a short, fixed duration to simulate UI/user delays.
     */
    void pause() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }
}
