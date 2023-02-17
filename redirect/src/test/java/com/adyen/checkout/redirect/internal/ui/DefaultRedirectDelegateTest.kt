/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/8/2022.
 */

package com.adyen.checkout.redirect.internal.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.test.TestRedirectHandler
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.redirect.RedirectConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultRedirectDelegateTest {

    private lateinit var redirectHandler: TestRedirectHandler
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultRedirectDelegate

    @BeforeEach
    fun beforeEach() {
        val configuration = RedirectConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            TEST_CLIENT_KEY
        ).build()
        redirectHandler = TestRedirectHandler()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        delegate = DefaultRedirectDelegate(
            ActionObserverRepository(),
            GenericComponentParamsMapper().mapToParams(configuration),
            redirectHandler,
            paymentDataRepository
        )
    }

    @Test
    fun `when handleAction called and RedirectHandler returns an error, then the error is propagated`() = runTest {
        val error = ComponentException("Failed to make redirect.")
        redirectHandler.exception = error

        delegate.exceptionFlow.test {
            delegate.handleAction(RedirectAction(paymentData = "paymentData"), Activity())

            assertEquals(error, awaitItem())
        }
    }

    @Test
    fun `when handleAction called with valid data, then no error is propagated`() = runTest {
        delegate.exceptionFlow.test {
            delegate.handleAction(RedirectAction(paymentData = "paymentData"), Activity())

            expectNoEvents()
        }
    }

    @Test
    fun `when handleIntent called and RedirectHandler returns an error, then the error is propagated`() = runTest {
        val error = ComponentException("Failed to parse redirect result.")
        redirectHandler.exception = error

        delegate.exceptionFlow.test {
            delegate.handleIntent(Intent())

            assertEquals(error, awaitItem())
        }
    }

    @Test
    fun `when handleIntent called with valid data, then the details are emitted`() = runTest {
        delegate.detailsFlow.test {
            delegate.handleAction(RedirectAction(paymentData = "paymentData"), Activity())
            delegate.handleIntent(Intent())

            with(awaitItem()) {
                assertEquals(TestRedirectHandler.REDIRECT_RESULT, details)
                assertEquals("paymentData", paymentData)
            }
        }
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
