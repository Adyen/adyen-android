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
import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.util.PdfOpener
import com.adyen.checkout.voucher.VoucherConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultVoucherDelegateTest(
    @Mock private val pdfOpener: PdfOpener,
    @Mock private val context: Context,
    @Mock private val activity: Activity
) {

    private lateinit var delegate: DefaultVoucherDelegate

    @BeforeEach
    fun beforeEach() {
        val configuration = VoucherConfiguration.Builder(Locale.getDefault(), Environment.TEST, TEST_CLIENT_KEY).build()
        delegate = DefaultVoucherDelegate(
            ActionObserverRepository(),
            GenericComponentParamsMapper(null, null).mapToParams(configuration, null),
            pdfOpener
        )
    }

    @Test
    fun `when handleAction called with valid data, then output data should be good`() = runTest {
        delegate.outputDataFlow.test {
            delegate.handleAction(
                VoucherAction(
                    paymentMethodType = PaymentMethodTypes.BACS,
                    downloadUrl = "download_url",
                    paymentData = "paymentData",
                    expiresAt = "now",
                    reference = "ref",
                    totalAmount = Amount("EUR", 1000)
                ),
                activity,
            )

            with(expectMostRecentItem()) {
                assertEquals(PaymentMethodTypes.BACS, paymentMethodType)
                assertEquals("download_url", downloadUrl)
                assertEquals("now", expiresAt)
                assertEquals("ref", reference)
                assertEquals(Amount("EUR", 1000), totalAmount)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("viewTypeSource")
    fun `when handling action for payment method, then the correct view type should be emitted`(
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
    fun `when handling action for unsupported payment method, then an error should be emitted`() = runTest {
        delegate.exceptionFlow.test {
            delegate.handleAction(
                VoucherAction(paymentMethodType = "something_that_doesn't_work", paymentData = "paymentData"),
                activity,
            )

            assert(expectMostRecentItem() is ComponentException)
        }
    }

    @Test
    fun `when download voucher is called, then pdf open should be called`() {
        delegate.handleAction(
            VoucherAction(
                paymentMethodType = PaymentMethodTypes.BACS,
                downloadUrl = "download_url",
                paymentData = "paymentData",
            ),
            activity,
        )
        delegate.storeVoucher(context)

        verify(pdfOpener).open(context, "download_url")
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"

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
        )
    }
}
