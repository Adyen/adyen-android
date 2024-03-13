/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/8/2022.
 */

package com.adyen.checkout.await.internal.ui

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.components.core.internal.test.TestStatusRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.test.LoggingExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class)
internal class DefaultAwaitDelegateTest {

    private lateinit var statusRepository: TestStatusRepository
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultAwaitDelegate

    @BeforeEach
    fun beforeEach() {
        statusRepository = TestStatusRepository()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        val configuration = CheckoutConfiguration(
            Environment.TEST,
            TEST_CLIENT_KEY,
        )
        delegate = DefaultAwaitDelegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = SavedStateHandle(),
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null),
            statusRepository = statusRepository,
            paymentDataRepository = paymentDataRepository,
        )
    }

    @Test
    fun `when  polling status, then output data will be emitted`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "pending")),
            Result.success(StatusResponse(resultCode = "finished")),
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.outputDataFlow.test {
            delegate.handleAction(AwaitAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

            skipItems(1)

            with(awaitItem()) {
                assertFalse(isValid)
                assertEquals("test", paymentMethodType)
            }

            with(awaitItem()) {
                assertTrue(isValid)
                assertEquals("test", paymentMethodType)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when polling is final, then details will be emitted`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.detailsFlow.test {
            delegate.handleAction(AwaitAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

            val expectedDetails = JSONObject().apply {
                put(DefaultAwaitDelegate.PAYLOAD_DETAILS_KEY, "testpayload")
            }

            with(awaitItem()) {
                assertEquals(expectedDetails.toString(), details.toString())
                assertEquals("paymentData", paymentData)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when polling fails, then an error is propagated`() = runTest {
        val error = IOException("test")
        statusRepository.pollingResults = listOf(Result.failure(error))
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.exceptionFlow.test {
            delegate.handleAction(AwaitAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

            assertEquals(error, awaitItem().cause)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when polling is final and payload is empty, then an error is propagated`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "")),
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.exceptionFlow.test {
            delegate.handleAction(AwaitAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

            assertTrue(awaitItem() is ComponentException)

            cancelAndIgnoreRemainingEvents()
        }
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
