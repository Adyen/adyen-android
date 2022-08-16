/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/8/2022.
 */

package com.adyen.checkout.qrcode

import app.cash.turbine.test
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.components.test.TestStatusRepository
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.qrcode.DefaultQRCodeDelegate.Companion.PAYLOAD_DETAILS_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultQRCodeDelegateTest(
    @Mock private val countDownTimer: QRCodeCountDownTimer
) {

    private lateinit var statusRepository: TestStatusRepository
    private lateinit var delegate: DefaultQRCodeDelegate

    @BeforeEach
    fun beforeEach() {
        statusRepository = TestStatusRepository()
        delegate = DefaultQRCodeDelegate(statusRepository, countDownTimer)
        Logger.setLogcatLevel(Logger.NONE)
    }

    @Test
    fun `when timer ticks, then left over time and progress are emitted`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.timerFlow.test {
            delegate.onTimerTick(10000)

            skipItems(1)

            assertEquals(TimerData(10000, 1), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when  polling status, then output data will be emitted`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "pending")),
            Result.success(StatusResponse(resultCode = "finished")),
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.outputDataFlow.test {
            delegate.handleAction(QrCodeAction(paymentMethodType = "test"), "paymentData")

            skipItems(1)

            awaitItem()!!.apply {
                assertFalse(isValid)
                assertEquals("test", paymentMethodType)
            }

            awaitItem()!!.apply {
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
            delegate.handleAction(QrCodeAction(paymentMethodType = "test"), "paymentData")

            skipItems(1)

            assertEquals("testpayload", awaitItem()!!.getString(PAYLOAD_DETAILS_KEY))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when polling fails, then an error is propagated`() = runTest {
        val error = IOException("test")
        statusRepository.pollingResults = listOf(Result.failure(error))
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.exceptionFlow.test {
            delegate.handleAction(QrCodeAction(paymentMethodType = "test"), "paymentData")

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
            delegate.handleAction(QrCodeAction(paymentMethodType = "test"), "paymentData")

            assertTrue(awaitItem() is ComponentException)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
