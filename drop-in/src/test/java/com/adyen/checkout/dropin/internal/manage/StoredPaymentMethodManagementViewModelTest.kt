/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/10/2026.
 */

package com.adyen.checkout.dropin.internal.manage

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredBLIKPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.DefaultPaymentMethodRepository
import com.adyen.checkout.dropin.internal.data.StoredPaymentMethodRemover
import com.adyen.checkout.dropin.internal.helper.PaymentMethodSupportCheck
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodExpiryCheck
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException
import java.util.Calendar
import java.util.GregorianCalendar

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, TestDispatcherExtension::class)
internal class StoredPaymentMethodManagementViewModelTest {

    @Test
    fun `when a stored payment method is removed, then it is no longer listed`() = runTest {
        val viewModel = createViewModel(
            remover = { Result.success(Unit) },
            coroutineScope = backgroundScope,
        )

        viewModel.viewState.test {
            assertEquals(listOf(FIRST_ID, SECOND_ID), expectMostRecentItem().cardIds())

            viewModel.removeStoredPaymentMethod(FIRST_ID)
            runCurrent()

            assertEquals(listOf(SECOND_ID), expectMostRecentItem().cardIds())
        }
    }

    @Test
    fun `when the removal fails, then the stored payment method stays listed and the error is shown`() = runTest {
        val viewModel = createViewModel(
            remover = { Result.failure(IOException("network down")) },
            coroutineScope = backgroundScope,
        )

        viewModel.viewState.test {
            assertFalse(expectMostRecentItem().showRemovalError)

            viewModel.removeStoredPaymentMethod(FIRST_ID)
            runCurrent()

            val viewState = expectMostRecentItem()
            assertEquals(listOf(FIRST_ID, SECOND_ID), viewState.cardIds())
            assertTrue(viewState.showRemovalError)
        }
    }

    @Test
    fun `when a removal is running, then only that item is shown as being removed`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val viewModel = createViewModel(
            remover = { response.await() },
            coroutineScope = backgroundScope,
        )

        viewModel.viewState.test {
            assertEquals(emptyList<String>(), expectMostRecentItem().removingCardIds())

            viewModel.removeStoredPaymentMethod(FIRST_ID)
            runCurrent()
            assertEquals(listOf(FIRST_ID), expectMostRecentItem().removingCardIds())

            response.complete(Result.success(Unit))
            runCurrent()
            val viewState = expectMostRecentItem()
            assertEquals(listOf(SECOND_ID), viewState.cardIds())
            assertEquals(emptyList<String>(), viewState.removingCardIds())
        }
    }

    @Test
    fun `when a removal is running from an earlier screen, then a new screen shows it as being removed`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val repository = createRepository(
            remover = { response.await() },
            coroutineScope = backgroundScope,
        )
        createViewModel(repository).removeStoredPaymentMethod(FIRST_ID)
        runCurrent()

        val reopened = createViewModel(repository)

        reopened.viewState.test {
            assertEquals(listOf(FIRST_ID), expectMostRecentItem().removingCardIds())
        }
    }

    @Test
    fun `when a removal from an earlier screen fails, then a new screen shows the error`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val repository = createRepository(
            remover = { response.await() },
            coroutineScope = backgroundScope,
        )
        val earlier = createViewModel(repository)
        earlier.removeStoredPaymentMethod(FIRST_ID)
        runCurrent()
        // Stands in for the shopper leaving the screen, which clears the view model of its nav entry.
        earlier.viewModelScope.cancel()

        val reopened = createViewModel(repository)

        reopened.viewState.test {
            assertFalse(expectMostRecentItem().showRemovalError)

            response.complete(Result.failure(IOException("network down")))
            runCurrent()

            val viewState = expectMostRecentItem()
            assertTrue(viewState.showRemovalError)
            assertEquals(emptyList<String>(), viewState.removingCardIds())
        }
    }

    @Test
    fun `when a new removal starts after a failure, then the error is hidden`() = runTest {
        val secondResponse = CompletableDeferred<Result<Unit>>()
        val viewModel = createViewModel(
            remover = {
                if (it.id == FIRST_ID) Result.failure(IOException("network down")) else secondResponse.await()
            },
            coroutineScope = backgroundScope,
        )

        viewModel.viewState.test {
            viewModel.removeStoredPaymentMethod(FIRST_ID)
            runCurrent()
            assertTrue(expectMostRecentItem().showRemovalError)

            viewModel.removeStoredPaymentMethod(SECOND_ID)
            runCurrent()

            assertFalse(expectMostRecentItem().showRemovalError)
        }
    }

    @Test
    fun `when the view model is cleared during the removal, then the stored payment method is still removed`() =
        runTest {
            val response = CompletableDeferred<Result<Unit>>()
            val repository = createRepository(
                remover = { response.await() },
                coroutineScope = backgroundScope,
            )
            val viewModel = createViewModel(repository)
            viewModel.removeStoredPaymentMethod(FIRST_ID)
            runCurrent()

            // Stands in for the shopper leaving the screen, which clears the view model of its nav entry.
            viewModel.viewModelScope.cancel()
            response.complete(Result.success(Unit))
            runCurrent()

            assertEquals(listOf(SECOND_ID), repository.storedPaymentMethods.value.map { it.id })
        }

    @Test
    fun `when there are cards and other stored payment methods, then the other section has a title`() = runTest {
        val viewModel = createViewModel(
            createRepository(
                storedPaymentMethods = listOf(storedCard(FIRST_ID), storedBlik(SECOND_ID)),
                coroutineScope = backgroundScope,
            ),
        )

        viewModel.viewState.test {
            assertTrue(expectMostRecentItem().showOthersSectionTitle)
        }
    }

    @Test
    fun `when there are only other stored payment methods, then the other section has no title`() = runTest {
        val viewModel = createViewModel(
            createRepository(storedPaymentMethods = listOf(storedBlik(SECOND_ID)), coroutineScope = backgroundScope),
        )

        viewModel.viewState.test {
            val viewState = expectMostRecentItem()
            assertEquals(listOf(SECOND_ID), viewState.others.map { it.id })
            assertFalse(viewState.showOthersSectionTitle)
        }
    }

    @Test
    fun `when a card is listed, then its removal is confirmed with its name and last four`() = runTest {
        val viewModel = createViewModel(createRepository(coroutineScope = backgroundScope))

        viewModel.viewState.test {
            assertEquals("Visa •••• 1234", expectMostRecentItem().cards.first().removalTitle)
        }
    }

    @Test
    fun `when stored payment methods are listed, then the list is not empty from the start`() = runTest {
        val viewModel = createViewModel(createRepository(coroutineScope = backgroundScope))

        assertFalse(viewModel.viewState.value.isEmpty)
    }

    @Test
    fun `when the last stored payment method is removed, then the list is empty`() = runTest {
        val viewModel = createViewModel(
            createRepository(storedPaymentMethods = listOf(storedCard(FIRST_ID)), coroutineScope = backgroundScope),
        )

        viewModel.viewState.test {
            assertFalse(expectMostRecentItem().isEmpty)

            viewModel.removeStoredPaymentMethod(FIRST_ID)
            runCurrent()

            assertTrue(expectMostRecentItem().isEmpty)
        }
    }

    @Test
    fun `when a card is expired, then only that card is shown as expired`() = runTest {
        val viewModel = createViewModel(
            createRepository(
                storedPaymentMethods = listOf(storedCard(FIRST_ID, expiryYear = "2025"), storedCard(SECOND_ID)),
                coroutineScope = backgroundScope,
            ),
        )

        viewModel.viewState.test {
            val expiredIds = expectMostRecentItem().cards.filter { it.isExpired }.map { it.id }
            assertEquals(listOf(FIRST_ID), expiredIds)
        }
    }

    @Test
    fun `when the screen is opened, then a rendered event is tracked`() = runTest {
        val analyticsManager = TestAnalyticsManager()

        createViewModel(createRepository(coroutineScope = backgroundScope), analyticsManager)

        analyticsManager.assertLastEventEquals(GenericEvents.rendered(component = "storedPaymentMethodManagement"))
    }

    @Test
    fun `when a removal is confirmed, then a click on the remove button is tracked`() = runTest {
        val analyticsManager = TestAnalyticsManager()
        val viewModel = createViewModel(createRepository(coroutineScope = backgroundScope), analyticsManager)

        viewModel.removeStoredPaymentMethod(FIRST_ID)

        analyticsManager.assertLastEventEquals(
            GenericEvents.clicked(
                component = "storedPaymentMethodManagement",
                target = "stored_payment_method_remove_button",
            ),
        )
    }

    private fun createViewModel(
        remover: StoredPaymentMethodRemover,
        coroutineScope: CoroutineScope,
    ) = createViewModel(createRepository(remover = remover, coroutineScope = coroutineScope))

    private fun createViewModel(
        repository: DefaultPaymentMethodRepository,
        analyticsManager: AnalyticsManager = TestAnalyticsManager(),
    ) = StoredPaymentMethodManagementViewModel(
        paymentMethodRepository = repository,
        paymentMethodSupportCheck = PaymentMethodSupportCheck(),
        expiryCheck = StoredPaymentMethodExpiryCheck { GregorianCalendar(2026, Calendar.OCTOBER, 22) },
        analyticsManager = analyticsManager,
    )

    private fun createRepository(
        remover: StoredPaymentMethodRemover = StoredPaymentMethodRemover { Result.success(Unit) },
        storedPaymentMethods: List<StoredPaymentMethod> = listOf(storedCard(FIRST_ID), storedCard(SECOND_ID)),
        coroutineScope: CoroutineScope,
    ) = DefaultPaymentMethodRepository(
        paymentMethods = emptyList(),
        storedPaymentMethods = storedPaymentMethods,
        storedPaymentMethodRemover = remover,
        coroutineScope = coroutineScope,
    )

    private fun StoredPaymentMethodManagementViewState.cardIds() = cards.map { it.id }

    private fun StoredPaymentMethodManagementViewState.removingCardIds() = cards.filter { it.isRemoving }.map { it.id }

    private fun storedCard(id: String, expiryYear: String = "2030") = StoredCardPaymentMethod(
        type = PaymentMethodTypes.SCHEME,
        id = id,
        name = "Visa",
        supportedShopperInteractions = listOf("Ecommerce"),
        brand = "visa",
        lastFour = "1234",
        expiryMonth = "01",
        expiryYear = expiryYear,
        holderName = null,
        fundingSource = null,
    )

    private fun storedBlik(id: String) = StoredBLIKPaymentMethod(
        type = PaymentMethodTypes.BLIK,
        name = "BLIK",
        id = id,
        supportedShopperInteractions = listOf("Ecommerce"),
    )

    private companion object {
        private const val FIRST_ID = "stored-id-1"
        private const val SECOND_ID = "stored-id-2"
    }
}
