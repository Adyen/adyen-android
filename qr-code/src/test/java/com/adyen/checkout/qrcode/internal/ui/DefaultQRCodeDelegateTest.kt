/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/8/2022.
 */

package com.adyen.checkout.qrcode.internal.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.QrCodeAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.data.api.StatusRepository
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.components.core.internal.test.TestStatusRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.TimerData
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.PermissionHandlerCallback
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.qrcode.internal.QRCodeCountDownTimer
import com.adyen.checkout.qrcode.internal.ui.model.QrCodeUIEvent
import com.adyen.checkout.qrcode.qrCode
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.RedirectHandler
import com.adyen.checkout.ui.core.internal.exception.PermissionRequestException
import com.adyen.checkout.ui.core.internal.test.TestRedirectHandler
import com.adyen.checkout.ui.core.internal.util.ImageSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultQRCodeDelegateTest(
    @Mock private val countDownTimer: QRCodeCountDownTimer,
    @Mock private val context: Context,
    @Mock private val imageSaver: ImageSaver
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var redirectHandler: TestRedirectHandler
    private lateinit var statusRepository: TestStatusRepository
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultQRCodeDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        statusRepository = TestStatusRepository()
        redirectHandler = TestRedirectHandler()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        val configuration = CheckoutConfiguration(
            Environment.TEST,
            TEST_CLIENT_KEY,
        ) {
            qrCode()
        }
        delegate = createDelegate(
            observerRepository = ActionObserverRepository(),
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null),
            statusRepository = statusRepository,
            statusCountDownTimer = countDownTimer,
            redirectHandler = redirectHandler,
            paymentDataRepository = paymentDataRepository,
            imageSaver = imageSaver,
        )
    }

    @Test
    fun `when observe is called, then observers are being added to the repository`() {
        val observerRepository = mock<ActionObserverRepository>()
        val delegate = createDelegate(
            observerRepository = observerRepository,
        )
        val lifecycleOwner = mock<LifecycleOwner>().apply {
            whenever(lifecycle).thenReturn(mock())
        }
        val coroutineScope = mock<CoroutineScope>()
        val callback = mock<(ActionComponentEvent) -> Unit>()

        delegate.observe(lifecycleOwner, coroutineScope, callback)

        verify(observerRepository).addObservers(
            detailsFlow = eq(delegate.detailsFlow),
            exceptionFlow = eq(delegate.exceptionFlow),
            permissionFlow = eq(delegate.permissionFlow),
            lifecycleOwner = eq(lifecycleOwner),
            coroutineScope = eq(coroutineScope),
            callback = eq(callback),
        )
    }

    @Test
    fun `when removeObserver is called, then observers are being removed`() {
        val observerRepository = mock<ActionObserverRepository>()
        val delegate = createDelegate(
            observerRepository = observerRepository,
        )

        delegate.removeObserver()

        verify(observerRepository).removeObservers()
    }

    @Test
    fun `when handleAction is called with unsupported action, then an error should be emitted`() = runTest {
        val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

        delegate.handleAction(
            createTestAction(),
            mock(),
        )

        assert(exceptionFlow.latestValue is ComponentException)
    }

    @Test
    fun `when handleAction is called with null payment data, then an error should be emitted`() = runTest {
        val exceptionFlow = delegate.exceptionFlow.test(testScheduler)
        delegate.handleAction(
            QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = null),
            mock(),
        )

        assert(exceptionFlow.latestValue is ComponentException)
    }

    @Nested
    @DisplayName("when in the QR code flow and")
    inner class QRCodeFlowTest {

        @Test
        fun `timer ticks, then left over time and progress are emitted`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val timerFlow = delegate.timerFlow.test(testScheduler)
            delegate.onTimerTick(10000)

            assertEquals(TimerData(10000, 1), timerFlow.latestValue)
        }

        @Test
        fun `polling status for pix is running, then output data will be emitted`() = runTest {
            statusRepository.pollingResults = listOf(
                Result.success(StatusResponse(resultCode = "pending")),
                Result.success(StatusResponse(resultCode = "finished")),
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputDataFlow = delegate.outputDataFlow.test(testScheduler)
            delegate.handleAction(
                QrCodeAction(
                    paymentMethodType = PaymentMethodTypes.PIX,
                    qrCodeData = "qrData",
                    paymentData = TEST_PAYMENT_DATA,
                ),
                Activity(),
            )

            with(outputDataFlow.values[1]) {
                assertFalse(isValid)
                assertEquals(PaymentMethodTypes.PIX, paymentMethodType)
                assertEquals("qrData", qrCodeData)
            }

            with(outputDataFlow.values[2]) {
                assertTrue(isValid)
                assertEquals(PaymentMethodTypes.PIX, paymentMethodType)
                assertEquals("qrData", qrCodeData)
            }
        }

        @Test
        fun `polling for pix is final, then details will be emitted`() = runTest {
            statusRepository.pollingResults = listOf(
                Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val detailsFlow = delegate.detailsFlow.test(testScheduler)

            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = TEST_PAYMENT_DATA),
                Activity(),
            )

            assertEquals(
                "testpayload",
                detailsFlow.latestValue.details?.getString(DefaultQRCodeDelegate.PAYLOAD_DETAILS_KEY),
            )
        }

        @Test
        fun `polling for pix fails, then an error is propagated`() = runTest {
            val error = IOException("test")
            statusRepository.pollingResults = listOf(Result.failure(error))
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = TEST_PAYMENT_DATA),
                Activity(),
            )

            assertEquals(error, exceptionFlow.latestValue.cause)
        }

        @Test
        fun `polling for pix is final and payload is empty, then an error is propagated`() = runTest {
            statusRepository.pollingResults = listOf(
                Result.success(StatusResponse(resultCode = "finished", payload = "")),
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = TEST_PAYMENT_DATA),
                Activity(),
            )

            assertTrue(exceptionFlow.latestValue is ComponentException)
        }

        @Test
        fun `handleAction is called, then simple qr view flow is updated`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val viewFlow = delegate.viewFlow.test(testScheduler)

            assertNull(viewFlow.latestValue)

            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = TEST_PAYMENT_DATA),
                Activity(),
            )

            assertEquals(QrCodeComponentViewType.SIMPLE_QR_CODE, viewFlow.latestValue)
        }

        @Test
        fun `polling status for payNow is running, then output data will be emitted`() = runTest {
            statusRepository.pollingResults = listOf(
                Result.success(StatusResponse(resultCode = "pending")),
                Result.success(StatusResponse(resultCode = "finished")),
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputDataFlow = delegate.outputDataFlow.test(testScheduler)
            delegate.handleAction(
                QrCodeAction(
                    paymentMethodType = PaymentMethodTypes.PAY_NOW,
                    qrCodeData = "qrData",
                    paymentData = TEST_PAYMENT_DATA,
                ),
                Activity(),
            )

            with(outputDataFlow.values[1]) {
                assertFalse(isValid)
                assertEquals(PaymentMethodTypes.PAY_NOW, paymentMethodType)
                assertEquals("qrData", qrCodeData)
            }

            with(outputDataFlow.values[2]) {
                assertTrue(isValid)
                assertEquals(PaymentMethodTypes.PAY_NOW, paymentMethodType)
                assertEquals("qrData", qrCodeData)
            }
        }

        @Test
        fun `polling for payNow is final, then details will be emitted`() = runTest {
            statusRepository.pollingResults = listOf(
                Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val detailsFlow = delegate.detailsFlow.test(testScheduler)
            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PAY_NOW, paymentData = TEST_PAYMENT_DATA),
                Activity(),
            )

            assertEquals(
                "testpayload",
                detailsFlow.latestValue.details?.getString(DefaultQRCodeDelegate.PAYLOAD_DETAILS_KEY),
            )
        }

        @Test
        fun `polling for payNow is final and payload is empty, then an error is propagated`() = runTest {
            statusRepository.pollingResults = listOf(
                Result.success(StatusResponse(resultCode = "finished", payload = "")),
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PAY_NOW, paymentData = TEST_PAYMENT_DATA),
                Activity(),
            )

            assertTrue(exceptionFlow.latestValue is ComponentException)
        }

        @Test
        fun `polling for payNow fails, then an error is propagated`() = runTest {
            val error = IOException("test")
            statusRepository.pollingResults = listOf(Result.failure(error))
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PAY_NOW, paymentData = TEST_PAYMENT_DATA),
                Activity(),
            )

            assertEquals(error, exceptionFlow.latestValue.cause)
        }

        @Test
        fun `handleAction is called, then full qr view flow is updated`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val viewFlow = delegate.viewFlow.test(testScheduler)

            assertNull(viewFlow.latestValue)

            delegate.handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PAY_NOW, paymentData = TEST_PAYMENT_DATA),
                Activity(),
            )

            assertEquals(QrCodeComponentViewType.FULL_QR_CODE, viewFlow.latestValue)
        }
    }

    @Nested
    @DisplayName("when in the redirect flow and")
    inner class RedirectFlowTest {

        @Test
        fun `handleAction is called and RedirectHandler returns an error, then the error is propagated`() = runTest {
            val error = ComponentException("Failed to make redirect.")
            redirectHandler.exception = error
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(
                QrCodeAction(
                    paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                    paymentData = TEST_PAYMENT_DATA,
                ),
                Activity(),
            )

            assertEquals(error, exceptionFlow.latestValue)
        }

        @Test
        fun `handleAction is called with valid data, then no error is propagated`() = runTest {
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleAction(
                QrCodeAction(
                    paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                    paymentData = TEST_PAYMENT_DATA,
                ),
                Activity(),
            )

            assertTrue(exceptionFlow.values.isEmpty())
        }

        @Test
        fun `handleIntent is called and RedirectHandler returns an error, then the error is propagated`() = runTest {
            val error = ComponentException("Failed to parse redirect result.")
            redirectHandler.exception = error
            val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

            delegate.handleIntent(Intent())

            assertEquals(error, exceptionFlow.latestValue)
        }

        @Test
        fun `handleIntent is called with valid data, then the details are emitted`() = runTest {
            val detailsFlow = delegate.detailsFlow.test(testScheduler)
            delegate.handleAction(QrCodeAction(paymentData = TEST_PAYMENT_DATA), Activity())
            delegate.handleIntent(Intent())

            with(detailsFlow.latestValue) {
                assertEquals(TestRedirectHandler.REDIRECT_RESULT, details)
                assertEquals(TEST_PAYMENT_DATA, paymentData)
            }
        }

        @Test
        fun `handleAction is called, then the view flow is updated`() = runTest {
            val viewFlow = delegate.viewFlow.test(testScheduler)

            assertNull(viewFlow.latestValue)

            delegate.handleAction(QrCodeAction(paymentData = TEST_PAYMENT_DATA), Activity())

            assertEquals(QrCodeComponentViewType.REDIRECT, viewFlow.latestValue)
        }
    }

    @Test
    fun `when refreshStatus is called, then status for statusRepository gets refreshed`() = runTest {
        val statusRepository = mock<StatusRepository> {
            on { poll(any(), any()) } doReturn flowOf()
        }
        val paymentData = "Payment Data"
        val delegate = createDelegate(
            statusRepository = statusRepository,
            paymentDataRepository = paymentDataRepository,
        ).apply {
            initialize(CoroutineScope(UnconfinedTestDispatcher()))
            handleAction(
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = paymentData),
                mock(),
            )
        }

        delegate.refreshStatus()

        verify(statusRepository).refreshStatus(paymentData)
    }

    @Test
    fun `when refreshStatus is called with no payment data, then status for statusRepository does not get refreshed`() =
        runTest {
            val statusRepository = mock<StatusRepository>()
            val delegate = createDelegate(
                statusRepository = statusRepository,
                paymentDataRepository = paymentDataRepository,
            ).apply {
                handleAction(
                    QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = null),
                    mock(),
                )
            }

            delegate.refreshStatus()

            verify(statusRepository, never()).refreshStatus(any())
        }

    @Test
    fun `when downloadQRImage is called with success, then Success gets emitted`() = runTest {
        whenever(imageSaver.saveImageFromUrl(any(), any(), any(), anyOrNull(), anyOrNull())).thenReturn(
            Result.success(Unit),
        )

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val eventFlow = delegate.eventFlow.test(testScheduler)
        val expectedResult = QrCodeUIEvent.QrImageDownloadResult.Success

        delegate.downloadQRImage(context)

        assertEquals(expectedResult, eventFlow.latestValue)
    }

    @Test
    fun `when downloadQRImage is called with permission exception, then PermissionDenied gets emitted`() = runTest {
        whenever(imageSaver.saveImageFromUrl(any(), any(), any(), anyOrNull(), anyOrNull())).thenReturn(
            Result.failure(PermissionRequestException("Error message for permission request exception")),
        )

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val eventFlow = delegate.eventFlow.test(testScheduler)

        val expectedResult = QrCodeUIEvent.QrImageDownloadResult.PermissionDenied

        delegate.downloadQRImage(context)

        assertEquals(expectedResult, eventFlow.latestValue)
    }

    @Test
    fun `when downloadQRImage is called with failure, then Success gets emitted`() = runTest {
        val throwable = CheckoutException("error")
        whenever(imageSaver.saveImageFromUrl(any(), any(), any(), anyOrNull(), anyOrNull())).thenReturn(
            Result.failure(throwable),
        )

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val eventFlow = delegate.eventFlow.test(testScheduler)
        val expectedResult = QrCodeUIEvent.QrImageDownloadResult.Failure(throwable)

        delegate.downloadQRImage(context)

        assertEquals(expectedResult, eventFlow.latestValue)
    }

    @Test
    fun `when requestPermission is called, then correct permission request data is being emitted`() = runTest {
        val requiredPermission = "Required Permission"
        val permissionCallback = mock<PermissionHandlerCallback>()

        val permissionFlow = delegate.permissionFlow.test(testScheduler)
        delegate.requestPermission(context, requiredPermission, permissionCallback)

        val mostRecentValue = permissionFlow.latestValue
        assertEquals(requiredPermission, mostRecentValue.requiredPermission)
        assertEquals(permissionCallback, mostRecentValue.permissionCallback)
    }

    @Test
    fun `when initializing and action is set, then state is restored`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
        )
        val savedStateHandle = SavedStateHandle().apply {
            set(
                DefaultQRCodeDelegate.ACTION_KEY,
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = "paymentData"),
            )
        }
        delegate = createDelegate(savedStateHandle = savedStateHandle)
        val detailsFlow = delegate.detailsFlow.test(testScheduler)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertTrue(detailsFlow.values.isNotEmpty())
    }

    @Test
    fun `when details are emitted, then state is cleared`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
        )
        val savedStateHandle = SavedStateHandle().apply {
            set(
                DefaultQRCodeDelegate.ACTION_KEY,
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = "paymentData"),
            )
        }
        delegate = createDelegate(savedStateHandle = savedStateHandle)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertNull(savedStateHandle[DefaultQRCodeDelegate.ACTION_KEY])
    }

    @Test
    fun `when an error is emitted, then state is cleared`() = runTest {
        val savedStateHandle = SavedStateHandle().apply {
            set(
                DefaultQRCodeDelegate.ACTION_KEY,
                QrCodeAction(paymentMethodType = PaymentMethodTypes.PIX, paymentData = null),
            )
        }
        delegate = createDelegate(savedStateHandle = savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertNull(savedStateHandle[DefaultQRCodeDelegate.ACTION_KEY])
    }

    @Test
    fun `when onCleared is called, observers are removed`() {
        val observerRepository = mock<ActionObserverRepository>()
        val countDownTimer = mock<QRCodeCountDownTimer>()
        val redirectHandler = mock<RedirectHandler>()
        val delegate = createDelegate(
            observerRepository = observerRepository,
            statusCountDownTimer = countDownTimer,
            redirectHandler = redirectHandler,
        )

        delegate.onCleared()

        verify(observerRepository).removeObservers()
        verify(countDownTimer).cancel()
        verify(redirectHandler).removeOnRedirectListener()
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when handleAction is called, then action event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val action = QrCodeAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                paymentData = TEST_PAYMENT_DATA,
            )

            delegate.handleAction(action, mock())

            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when downloadQRImage is called, then download event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.handleAction(
                QrCodeAction(
                    paymentMethodType = PaymentMethodTypes.PIX,
                    qrCodeData = TEST_QR_CODE_DATA,
                    paymentData = TEST_PAYMENT_DATA,
                ),
                mock(),
            )

            delegate.downloadQRImage(context)

            val expectedEvent = GenericEvents.download(
                component = PaymentMethodTypes.PIX,
                target = DefaultQRCodeDelegate.ANALYTICS_TARGET_QR_BUTTON,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    private fun createTestAction(
        type: String = "test",
        paymentData: String = "paymentData",
        paymentMethodType: String = "paymentMethodType",
    ) = object : Action() {
        override var type: String? = type
        override var paymentData: String? = paymentData
        override var paymentMethodType: String? = paymentMethodType
        override fun writeToParcel(dest: Parcel, flags: Int) = Unit
    }

    @Suppress("LongParameterList")
    private fun createDelegate(
        observerRepository: ActionObserverRepository = mock(),
        componentParams: GenericComponentParams = mock(),
        statusRepository: StatusRepository = this.statusRepository,
        statusCountDownTimer: QRCodeCountDownTimer = mock(),
        redirectHandler: RedirectHandler = mock(),
        paymentDataRepository: PaymentDataRepository = mock(),
        imageSaver: ImageSaver = mock(),
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) = DefaultQRCodeDelegate(
        observerRepository = observerRepository,
        savedStateHandle = savedStateHandle,
        componentParams = componentParams,
        statusRepository = statusRepository,
        statusCountDownTimer = statusCountDownTimer,
        redirectHandler = redirectHandler,
        paymentDataRepository = paymentDataRepository,
        imageSaver = imageSaver,
        analyticsManager = analyticsManager,
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_ACTION_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_PAYMENT_DATA = "TEST_PAYMENT_DATA"
        private const val TEST_QR_CODE_DATA = "TEST_QR_CODE_DATA"
    }
}
