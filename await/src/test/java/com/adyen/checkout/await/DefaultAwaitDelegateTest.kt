/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/8/2022.
 */

package com.adyen.checkout.await

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.await.DefaultAwaitDelegate.Companion.PAYLOAD_DETAILS_KEY
import com.adyen.checkout.components.model.payments.response.AwaitAction
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.components.test.TestStatusRepository
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.Logger
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultAwaitDelegateTest {

    private lateinit var statusRepository: TestStatusRepository
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultAwaitDelegate

    @BeforeEach
    fun beforeEach() {
        statusRepository = TestStatusRepository()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        delegate = DefaultAwaitDelegate(statusRepository, paymentDataRepository)
        Logger.setLogcatLevel(Logger.NONE)
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

            with(requireNotNull(awaitItem())) {
                assertFalse(isValid)
                assertEquals("test", paymentMethodType)
            }

            with(requireNotNull(awaitItem())) {
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
                put(PAYLOAD_DETAILS_KEY, "testpayload")
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
}
