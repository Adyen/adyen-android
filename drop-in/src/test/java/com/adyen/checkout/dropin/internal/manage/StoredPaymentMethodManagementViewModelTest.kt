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
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.DefaultPaymentMethodRepository
import com.adyen.checkout.dropin.internal.data.StoredPaymentMethodRemover
import com.adyen.checkout.dropin.internal.helper.PaymentMethodSupportCheck
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException

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
    fun `when the removal fails, then the stored payment method stays listed`() = runTest {
        val viewModel = createViewModel(
            remover = { Result.failure(IOException("network down")) },
            coroutineScope = backgroundScope,
        )

        viewModel.viewState.test {
            assertEquals(listOf(FIRST_ID, SECOND_ID), expectMostRecentItem().cardIds())

            viewModel.removeStoredPaymentMethod(FIRST_ID)
            runCurrent()

            expectNoEvents()
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

    private fun createViewModel(
        remover: StoredPaymentMethodRemover,
        coroutineScope: CoroutineScope,
    ) = createViewModel(createRepository(remover, coroutineScope))

    private fun createViewModel(repository: DefaultPaymentMethodRepository) = StoredPaymentMethodManagementViewModel(
        paymentMethodRepository = repository,
        paymentMethodSupportCheck = PaymentMethodSupportCheck(),
    )

    private fun createRepository(
        remover: StoredPaymentMethodRemover,
        coroutineScope: CoroutineScope,
    ) = DefaultPaymentMethodRepository(
        paymentMethods = emptyList(),
        storedPaymentMethods = listOf(storedCard(FIRST_ID), storedCard(SECOND_ID)),
        storedPaymentMethodRemover = remover,
        coroutineScope = coroutineScope,
    )

    private fun StoredPaymentMethodManagementViewState.cardIds() = cards.map { it.id }

    private fun storedCard(id: String) = StoredCardPaymentMethod(
        type = PaymentMethodTypes.SCHEME,
        id = id,
        name = "Visa",
        supportedShopperInteractions = listOf("Ecommerce"),
        brand = "visa",
        lastFour = "1234",
        expiryMonth = "01",
        expiryYear = "2030",
        holderName = null,
        fundingSource = null,
    )

    private companion object {
        private const val FIRST_ID = "stored-id-1"
        private const val SECOND_ID = "stored-id-2"
    }
}
