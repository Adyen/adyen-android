/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/8/2022.
 */

package com.adyen.checkout.qrcode

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.test.TestStatusRepository
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.qrcode.DefaultQRCodeDelegate.Companion.PAYLOAD_DETAILS_KEY
import com.adyen.checkout.redirect.test.TestRedirectHandler
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

    private lateinit var redirectHandler: TestRedirectHandler
    private lateinit var statusRepository: TestStatusRepository
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultQRCodeDelegate

    @BeforeEach
    fun beforeEach() {
        statusRepository = TestStatusRepository()
        redirectHandler = TestRedirectHandler()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        delegate = DefaultQRCodeDelegate(statusRepository, countDownTimer, redirectHandler, paymentDataRepository)
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
            delegate.handleAction(
                QrCodeAction(
                    paymentMethodType = PaymentMethodTypes.PIX,
                    qrCodeData = "qrData",
                    paymentData = "paymentData"
                ),
                Activity(),
            )

            skipItems(1)

            with(requireNotNull(awaitItem())) {
                assertFalse(isValid)
                assertEquals(PaymentMethodTypes.PIX, paymentMethodType)
                assertEquals("qrData", qrCodeData)
            }

            with(requireNotNull(awaitItem())) {
                assertTrue(isValid)
                assertEquals(PaymentMethodTypes.PIX, paymentMethodType)
                assertEquals("qrData", qrCodeData)
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
            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = "paymentData"),
                Activity(),
            )

            assertEquals("testpayload", awaitItem().details?.getString(PAYLOAD_DETAILS_KEY))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when polling fails, then an error is propagated`() = runTest {
        val error = IOException("test")
        statusRepository.pollingResults = listOf(Result.failure(error))
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.exceptionFlow.test {
            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = "paymentData"),
                Activity(),
            )

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
            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = "paymentData"),
                Activity(),
            )

            assertTrue(awaitItem() is ComponentException)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when handleAction called and RedirectHandler returns an error, then the error is propagated`() = runTest {
        val error = ComponentException("Failed to make redirect.")
        redirectHandler.exception = error

        delegate.exceptionFlow.test {
            delegate.handleAction(QrCodeAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

            assertEquals(error, awaitItem())
        }
    }

    @Test
    fun `when handleAction called with valid data, then no error is propagated`() = runTest {
        delegate.exceptionFlow.test {
            delegate.handleAction(QrCodeAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

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
            delegate.handleAction(QrCodeAction(paymentData = "paymentData"), Activity())
            delegate.handleIntent(Intent())

            with(awaitItem()) {
                assertEquals(TestRedirectHandler.REDIRECT_RESULT, details)
                assertEquals("paymentData", paymentData)
            }
        }
    }
}
