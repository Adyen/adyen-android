/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/10/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.TestCheckoutContext
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import com.adyen.checkout.dropin.internal.data.api.DisableTokenRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Locale

@ExtendWith(MockitoExtension::class)
internal class DefaultStoredPaymentMethodRemoverTest(
    @param:Mock private val disableTokenRepository: DisableTokenRepository,
) {

    @Test
    fun `when the context is sessions, then the stored payment method is removed from the session`() = runTest {
        whenever(disableTokenRepository.removeStoredPaymentMethod(any(), any())) doReturn Result.success(Unit)
        val remover = createRemover(sessionsContext())

        val result = remover.remove(STORED_PAYMENT_METHOD)

        assertTrue(result.isSuccess)
        verify(disableTokenRepository).removeStoredPaymentMethod(CHECKOUT_SESSION, STORED_ID)
    }

    @Test
    fun `when the context is sessions and the removal fails, then the failure is returned`() = runTest {
        val error = IOException("network down")
        whenever(disableTokenRepository.removeStoredPaymentMethod(any(), any())) doReturn Result.failure(error)
        val remover = createRemover(sessionsContext())

        val result = remover.remove(STORED_PAYMENT_METHOD)

        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `when the context is advanced, then a failure is returned and the session is not called`() = runTest {
        val remover = createRemover(advancedContext())

        val result = remover.remove(STORED_PAYMENT_METHOD)

        assertTrue(result.isFailure)
        verifyNoInteractions(disableTokenRepository)
    }

    private fun createRemover(checkoutContext: CheckoutContext) = DefaultStoredPaymentMethodRemover(
        checkoutContext = checkoutContext,
        disableTokenRepository = disableTokenRepository,
    )

    private fun sessionsContext() = TestCheckoutContext.sessions(
        checkoutSession = CHECKOUT_SESSION,
        checkoutConfiguration = CHECKOUT_CONFIGURATION,
    )

    private fun advancedContext() = TestCheckoutContext.advanced(
        paymentMethods = PaymentMethods(),
        checkoutConfiguration = CHECKOUT_CONFIGURATION,
    )

    companion object {
        private const val STORED_ID = "stored-id"
        private const val CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"

        private val CHECKOUT_CONFIGURATION = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = CLIENT_KEY,
            shopperLocale = Locale.US,
        )

        private val CHECKOUT_SESSION = CheckoutSession(
            sessionSetupResponse = SessionSetupResponse(
                id = "session-id",
                sessionData = "session-data",
                amount = null,
                expiresAt = "2026-10-16T12:00:00Z",
                paymentMethods = null,
                returnUrl = null,
                configuration = null,
                shopperLocale = null,
            ),
            order = null,
            environment = Environment.TEST,
            clientKey = CLIENT_KEY,
        )

        private val STORED_PAYMENT_METHOD = StoredCardPaymentMethod(
            type = "scheme",
            id = STORED_ID,
            name = "MasterCard",
            supportedShopperInteractions = emptyList(),
            brand = "mc",
            lastFour = "1234",
            expiryMonth = "01",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        )
    }
}
