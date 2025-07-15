/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher.internal.ui

import android.app.Activity
import android.content.Context
import android.os.Parcel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.core.old.PermissionHandlerCallback
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.exception.PermissionRequestException
import com.adyen.checkout.ui.core.old.internal.util.ImageSaver
import com.adyen.checkout.ui.core.old.internal.util.PdfOpener
import com.adyen.checkout.voucher.internal.ui.model.VoucherStoreAction
import com.adyen.checkout.voucher.internal.ui.model.VoucherUIEvent
import com.adyen.checkout.voucher.voucher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultVoucherDelegateTest(
    @Mock private val observerRepository: ActionObserverRepository,
    @Mock private val pdfOpener: PdfOpener,
    @Mock private val context: Context,
    @Mock private val activity: Activity,
    @Mock private val imageSaver: ImageSaver,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultVoucherDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        delegate = createDelegate()
    }

    @Test
    fun `when observe is called, then observers are being added to the repository`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val coroutineScope = mock<CoroutineScope>()
        val callback = mock<(ActionComponentEvent) -> Unit>()

        delegate.observe(lifecycleOwner, coroutineScope, callback)

        verify(observerRepository).addObservers(
            detailsFlow = eq(null),
            exceptionFlow = eq(delegate.exceptionFlow),
            permissionFlow = eq(delegate.permissionFlow),
            lifecycleOwner = eq(lifecycleOwner),
            coroutineScope = eq(coroutineScope),
            callback = eq(callback),
        )
    }

    @Test
    fun `when removeObserver is called, then observers are being removed`() {
        delegate.removeObserver()

        verify(observerRepository).removeObservers()
    }

    @Test
    fun `when handleAction called with unsupported action, then an error should be emitted`() = runTest {
        delegate.exceptionFlow.test {
            delegate.handleAction(
                createTestAction(),
                activity,
            )

            assert(expectMostRecentItem() is ComponentException)
        }
    }

    @Test
    fun `when handleAction called with valid data including url, then output data should be good`() = runTest {
        delegate.outputDataFlow.test {
            delegate.handleAction(
                VoucherAction(
                    paymentMethodType = PaymentMethodTypes.BACS,
                    downloadUrl = "download_url",
                    paymentData = "paymentData",
                    expiresAt = "now",
                    reference = "ref",
                    totalAmount = Amount("EUR", 1000),
                ),
                activity,
            )

            with(expectMostRecentItem()) {
                assertEquals(PaymentMethodTypes.BACS, paymentMethodType)
                assertEquals(VoucherStoreAction.DownloadPdf("download_url"), storeAction)
                assertEquals("ref", reference)
                assertEquals(Amount("EUR", 1000), totalAmount)
            }
        }
    }

    @Test
    fun `when handleAction called with valid data not including url, then output data should be good`() = runTest {
        delegate.outputDataFlow.test {
            delegate.handleAction(
                VoucherAction(
                    paymentMethodType = PaymentMethodTypes.MULTIBANCO,
                    downloadUrl = null,
                    paymentData = "paymentData",
                    expiresAt = "now",
                    reference = "ref",
                    totalAmount = Amount("EUR", 1000),
                ),
                activity,
            )

            with(expectMostRecentItem()) {
                assertEquals(PaymentMethodTypes.MULTIBANCO, paymentMethodType)
                assertEquals(VoucherStoreAction.SaveAsImage, storeAction)
                assertEquals("ref", reference)
                assertEquals(Amount("EUR", 1000), totalAmount)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("viewTypeSource")
    fun `when handleAction called for payment method, then the correct view type should be emitted`(
        paymentMethodType: String,
        expectedViewType: ComponentViewType
    ) = runTest {
        delegate.viewFlow.test {
            delegate.handleAction(
                VoucherAction(paymentMethodType = paymentMethodType, paymentData = "paymentData"),
                activity,
            )

            assertEquals(expectedViewType, expectMostRecentItem())
        }
    }

    @Test
    fun `when handleAction called for unsupported payment method, then an error should be emitted`() = runTest {
        delegate.exceptionFlow.test {
            delegate.handleAction(
                VoucherAction(paymentMethodType = "something_that_doesn't_work", paymentData = "paymentData"),
                activity,
            )

            assert(expectMostRecentItem() is ComponentException)
        }
    }

    @Test
    fun `when downloadVoucher is called, then pdf open should be called`() {
        delegate.handleAction(
            VoucherAction(
                paymentMethodType = PaymentMethodTypes.BACS,
                downloadUrl = "download_url",
                paymentData = "paymentData",
            ),
            activity,
        )
        delegate.downloadVoucher(context)

        verify(pdfOpener).open(context, "download_url")
    }

    @Test
    fun `when downloadVoucher is called with no url, then exception should be emitted`() = runTest {
        delegate.exceptionFlow.test {
            whenever(pdfOpener.open(any(), eq(""))).thenThrow(IllegalStateException::class.java)
            delegate.handleAction(
                VoucherAction(
                    paymentMethodType = PaymentMethodTypes.MULTIBANCO,
                    downloadUrl = null,
                    paymentData = "paymentData",
                ),
                activity,
            )
            delegate.downloadVoucher(context)

            assert(expectMostRecentItem() is ComponentException)
        }
    }

    @Test
    fun `when saveVoucherAsImage is called, then correct ui event should be emitted`() = runTest {
        whenever(imageSaver.saveImageFromView(any(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(
            Result.success(Unit),
        )

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.eventFlow.test {
            val expectedResult = VoucherUIEvent.Success

            delegate.saveVoucherAsImage(context, mock())

            assertEquals(expectedResult, expectMostRecentItem())
        }
    }

    @Test
    fun `when saveVoucherAsImage is called with permission exception, then permission denied ui event should be emitted`() =
        runTest {
            whenever(
                imageSaver.saveImageFromView(
                    any(),
                    any(),
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(
                Result.failure(PermissionRequestException("Error message for permission request exception")),
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.eventFlow.test {
                val expectedResult = VoucherUIEvent.PermissionDenied

                delegate.saveVoucherAsImage(context, mock())

                assertEquals(expectedResult, expectMostRecentItem())
            }
        }

    @Test
    fun `when saveVoucherAsImage is called with error, then failure ui event should be emitted`() = runTest {
        val throwable = ComponentException("Error message")
        whenever(imageSaver.saveImageFromView(any(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(
            Result.failure(throwable),
        )

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.eventFlow.test {
            val expectedResult = VoucherUIEvent.Failure(throwable)

            delegate.saveVoucherAsImage(context, mock())

            assertEquals(expectedResult, expectMostRecentItem())
        }
    }

    @Test
    fun `when saveVoucherAsImage is called, then permission request should be emitted`() = runTest {
        val requiredPermission = "Required Permission"
        val permissionCallback = mock<PermissionHandlerCallback>()

        delegate.permissionFlow.test {
            delegate.requestPermission(context, requiredPermission, permissionCallback)

            val mostRecentValue = expectMostRecentItem()
            assertEquals(requiredPermission, mostRecentValue.requiredPermission)
            assertEquals(permissionCallback, mostRecentValue.permissionCallback)
        }
    }

    @Test
    fun `when initializing and action is set, then state is restored`() = runTest {
        val savedStateHandle = SavedStateHandle().apply {
            set(
                DefaultVoucherDelegate.ACTION_KEY,
                VoucherAction(paymentMethodType = PaymentMethodTypes.MULTIBANCO, paymentData = "paymentData"),
            )
        }
        delegate = createDelegate(savedStateHandle)
        val viewFlow = delegate.viewFlow.test(testScheduler)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertNotNull(viewFlow.latestValue)
    }

    @Test
    fun `when an error is emitted, then state is cleared`() = runTest {
        val savedStateHandle = SavedStateHandle().apply {
            set(
                DefaultVoucherDelegate.ACTION_KEY,
                VoucherAction(paymentMethodType = "not a voucher", paymentData = "paymentData"),
            )
        }
        delegate = createDelegate(savedStateHandle)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertNull(savedStateHandle[DefaultVoucherDelegate.ACTION_KEY])
    }

    @Test
    fun `when onCleared is called, observers are removed`() {
        delegate.onCleared()

        verify(observerRepository).removeObservers()
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when handleAction is called, then action event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val action = VoucherAction(
                paymentMethodType = PaymentMethodTypes.BACS,
                type = TEST_ACTION_TYPE,
            )

            delegate.handleAction(action, activity)

            val expectedEvent = GenericEvents.action(
                component = PaymentMethodTypes.BACS,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    private fun createDelegate(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): DefaultVoucherDelegate {
        val configuration = CheckoutConfiguration(Environment.TEST, TEST_CLIENT_KEY) {
            voucher()
        }

        return DefaultVoucherDelegate(
            observerRepository = observerRepository,
            savedStateHandle = savedStateHandle,
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null),
            pdfOpener = pdfOpener,
            imageSaver = imageSaver,
            analyticsManager = analyticsManager,
        )
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

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_ACTION_TYPE = "TEST_PAYMENT_METHOD_TYPE"

        @JvmStatic
        fun viewTypeSource() = listOf(
            // PaymentMethodType, VoucherComponentViewType
            arguments(PaymentMethodTypes.BACS, VoucherComponentViewType.SIMPLE_VOUCHER),
            arguments(PaymentMethodTypes.BOLETOBANCARIO, VoucherComponentViewType.FULL_VOUCHER),
            arguments(PaymentMethodTypes.BOLETOBANCARIO_BANCODOBRASIL, VoucherComponentViewType.FULL_VOUCHER),
            arguments(PaymentMethodTypes.BOLETOBANCARIO_BRADESCO, VoucherComponentViewType.FULL_VOUCHER),
            arguments(PaymentMethodTypes.BOLETOBANCARIO_HSBC, VoucherComponentViewType.FULL_VOUCHER),
            arguments(PaymentMethodTypes.BOLETOBANCARIO_ITAU, VoucherComponentViewType.FULL_VOUCHER),
            arguments(PaymentMethodTypes.BOLETOBANCARIO_SANTANDER, VoucherComponentViewType.FULL_VOUCHER),
            arguments(PaymentMethodTypes.BOLETO_PRIMEIRO_PAY, VoucherComponentViewType.FULL_VOUCHER),
            arguments(PaymentMethodTypes.MULTIBANCO, VoucherComponentViewType.FULL_VOUCHER),
        )
    }
}
