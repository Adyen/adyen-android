/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/8/2022.
 */

package com.adyen.checkout.qrcode

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.test.TestRedirectHandler
import com.adyen.checkout.components.test.TestStatusRepository
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.FileDownloader
import com.adyen.checkout.qrcode.DefaultQRCodeDelegate.Companion.PAYLOAD_DETAILS_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultQRCodeDelegateTest(
    @Mock private val countDownTimer: QRCodeCountDownTimer,
    @Mock private val context: Context
) {

    private lateinit var redirectHandler: TestRedirectHandler
    private lateinit var statusRepository: TestStatusRepository
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultQRCodeDelegate
    private lateinit var fileDownloader: FileDownloader

    @BeforeEach
    fun beforeEach() {
        fileDownloader = spy(FileDownloader(context))
        statusRepository = TestStatusRepository()
        redirectHandler = TestRedirectHandler()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        val configuration = QRCodeConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            TEST_CLIENT_KEY
        ).build()
        delegate = DefaultQRCodeDelegate(
            observerRepository = ActionObserverRepository(),
            componentParams = GenericComponentParamsMapper(null).mapToParams(configuration),
            statusRepository = statusRepository,
            statusCountDownTimer = countDownTimer,
            redirectHandler = redirectHandler,
            paymentDataRepository = paymentDataRepository,
            fileDownloader = fileDownloader
        )
        Logger.setLogcatLevel(Logger.NONE)
    }

    @Nested
    @DisplayName("when in the QR code flow and")
    inner class QRCodeFlowTest {

        @Test
        fun `timer ticks, then left over time and progress are emitted`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.timerFlow.test {
                delegate.onTimerTick(10000)

                skipItems(1)

                assertEquals(TimerData(10000, 1), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `polling status is running, then output data will be emitted`() = runTest {
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

                with(awaitItem()) {
                    assertFalse(isValid)
                    assertEquals(PaymentMethodTypes.PIX, paymentMethodType)
                    assertEquals("qrData", qrCodeData)
                }

                with(awaitItem()) {
                    assertTrue(isValid)
                    assertEquals(PaymentMethodTypes.PIX, paymentMethodType)
                    assertEquals("qrData", qrCodeData)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `polling is final, then details will be emitted`() = runTest {
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
        fun `polling fails, then an error is propagated`() = runTest {
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
        fun `polling is final and payload is empty, then an error is propagated`() = runTest {
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
        fun `handleAction called, then simple qr view flow is updated`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.viewFlow.test {
                assertNull(awaitItem())

                delegate.handleAction(
                    QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = "paymentData"),
                    Activity(),
                )

                assertEquals(awaitItem(), QrCodeComponentViewType.SIMPLE_QR_CODE)
            }
        }

        @Test
        fun `handleAction called, then full qr view flow is updated`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.viewFlow.test {
                assertNull(awaitItem())

                delegate.handleAction(
                    QrCodeAction(paymentMethodType = PaymentMethodTypes.PAY_NOW, paymentData = "paymentData"),
                    Activity(),
                )

                assertEquals(awaitItem(), QrCodeComponentViewType.FULL_QR_CODE)
            }
        }
    }

    @Nested
    @DisplayName("when in the redirect flow and")
    inner class RedirectFlowTest {

        @Test
        fun `handleAction called and RedirectHandler returns an error, then the error is propagated`() = runTest {
            val error = ComponentException("Failed to make redirect.")
            redirectHandler.exception = error

            delegate.exceptionFlow.test {
                delegate.handleAction(QrCodeAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

                assertEquals(error, awaitItem())
            }
        }

        @Test
        fun `handleAction called with valid data, then no error is propagated`() = runTest {
            delegate.exceptionFlow.test {
                delegate.handleAction(QrCodeAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

                expectNoEvents()
            }
        }

        @Test
        fun `handleIntent called and RedirectHandler returns an error, then the error is propagated`() = runTest {
            val error = ComponentException("Failed to parse redirect result.")
            redirectHandler.exception = error

            delegate.exceptionFlow.test {
                delegate.handleIntent(Intent())

                assertEquals(error, awaitItem())
            }
        }

        @Test
        fun `handleIntent called with valid data, then the details are emitted`() = runTest {
            delegate.detailsFlow.test {
                delegate.handleAction(QrCodeAction(paymentData = "paymentData"), Activity())
                delegate.handleIntent(Intent())

                with(awaitItem()) {
                    assertEquals(TestRedirectHandler.REDIRECT_RESULT, details)
                    assertEquals("paymentData", paymentData)
                }
            }
        }

        @Test
        fun `handleAction called, then the view flow is updated`() = runTest {
            delegate.viewFlow.test {
                assertNull(awaitItem())

                delegate.handleAction(QrCodeAction(paymentData = "paymentData"), Activity())

                assertEquals(awaitItem(), QrCodeComponentViewType.REDIRECT)
            }
        }
    }

    @Test
    fun `download qr image successfully`(): Unit = runBlocking {
        val expectedResult = Result.success(Unit)
        whenever(fileDownloader.download(anyString(), anyString(), anyString(), anyString())) doReturn expectedResult

        val actualResult = delegate.downloadQRImage()

        assertEquals(expectedResult.isSuccess, actualResult.isSuccess)
        verify(fileDownloader).download(anyString(), anyString(), anyString(), anyString()).isSuccess
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
