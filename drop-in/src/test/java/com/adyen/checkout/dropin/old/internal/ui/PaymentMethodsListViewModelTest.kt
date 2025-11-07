/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.ui

import android.app.Application
import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.dropin.old.internal.ConfigurationProvider
import com.adyen.checkout.dropin.old.internal.DataProvider
import com.adyen.checkout.dropin.old.internal.Helpers.mapToPaymentMethodModelList
import com.adyen.checkout.dropin.old.internal.Helpers.mapToStoredPaymentMethodsModelList
import com.adyen.checkout.dropin.old.internal.ui.model.DropInParamsMapper
import com.adyen.checkout.dropin.old.internal.ui.model.GiftCardPaymentMethodModel
import com.adyen.checkout.dropin.old.internal.ui.model.OrderModel
import com.adyen.checkout.dropin.old.internal.ui.model.PaymentMethodHeader
import com.adyen.checkout.dropin.old.internal.ui.model.PaymentMethodModel
import com.adyen.checkout.dropin.old.internal.ui.model.PaymentMethodNote
import com.adyen.checkout.dropin.old.internal.ui.model.StoredPaymentMethodModel
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class PaymentMethodsListViewModelTest(
    @Mock private val application: Application
) {

    private lateinit var viewModel: PaymentMethodsListViewModel

    @BeforeEach
    fun setup() {
        whenever(application.getString(any(), any())) doReturn "string"
        viewModel = createViewModel()
    }

    @Test
    fun `test remove stored payment method success`() = runTest {
        viewModel.paymentMethodsFlow.test {
            val paymentMethods = DataProvider.getStoredPaymentMethods().mapToStoredPaymentMethodsModelList(true)
            val storedPaymentMethod = paymentMethods[0]

            viewModel.removePaymentMethodWithId(storedPaymentMethod.id)

            with(expectMostRecentItem()) {
                assertFalse(contains(storedPaymentMethod))
            }
        }
    }

    @Test
    fun `test get payment method from payment method model success`() {
        val paymentMethod = PaymentMethod(type = "test", name = "Test pm")
        val paymentMethods = listOf(paymentMethod)
        viewModel = createViewModel(
            paymentMethods = paymentMethods,
        )
        val paymentMethodModelList = paymentMethods.mapToPaymentMethodModelList()

        val actual = viewModel.getPaymentMethod(paymentMethodModelList[0])

        assertEquals(paymentMethod, actual)
    }

    @Nested
    @DisplayName("test payment method flow when")
    inner class PaymentMethodFlow {

        @Test
        fun `all payment methods available`() = runTest {
            viewModel.paymentMethodsFlow.test {
                with(expectMostRecentItem()) {
                    assertTrue(filterIsInstance<StoredPaymentMethodModel>().isNotEmpty())
                    assertTrue(filterIsInstance<PaymentMethodModel>().isNotEmpty())
                    assertTrue(filterIsInstance<PaymentMethodHeader>().isNotEmpty())
                    assertTrue(filterIsInstance<GiftCardPaymentMethodModel>().isNotEmpty())
                    assertTrue(filterIsInstance<PaymentMethodNote>().isNotEmpty())
                }
            }
        }

        @Test
        fun `payment methods list is empty, then payment method flow won't contain payment methods`() = runTest {
            viewModel = createViewModel(paymentMethods = emptyList())

            viewModel.paymentMethodsFlow.test {
                with(expectMostRecentItem()) {
                    assertTrue(filterIsInstance<StoredPaymentMethodModel>().isNotEmpty())
                    assertTrue(filterIsInstance<PaymentMethodModel>().isEmpty())
                    assertTrue(filterIsInstance<PaymentMethodHeader>().isNotEmpty())
                    assertTrue(filterIsInstance<GiftCardPaymentMethodModel>().isNotEmpty())
                    assertTrue(filterIsInstance<PaymentMethodNote>().isNotEmpty())
                }
            }
        }

        @Test
        fun `stored payment methods list is empty, then payment method flow won't contain stored payment methods`() =
            runTest {
                viewModel = createViewModel(storedPaymentMethods = emptyList())

                viewModel.paymentMethodsFlow.test {
                    with(expectMostRecentItem()) {
                        assertTrue(filterIsInstance<StoredPaymentMethodModel>().isEmpty())
                        assertTrue(filterIsInstance<PaymentMethodModel>().isNotEmpty())
                        assertTrue(filterIsInstance<PaymentMethodHeader>().isNotEmpty())
                        assertTrue(filterIsInstance<GiftCardPaymentMethodModel>().isNotEmpty())
                        assertTrue(filterIsInstance<PaymentMethodNote>().isNotEmpty())
                    }
                }
            }

        @Test
        fun `order is null, then payment method flow won't contain gift cards or payment method note`() = runTest {
            viewModel = createViewModel(order = null)

            viewModel.paymentMethodsFlow.test {
                with(expectMostRecentItem()) {
                    assertTrue(filterIsInstance<StoredPaymentMethodModel>().isNotEmpty())
                    assertTrue(filterIsInstance<PaymentMethodModel>().isNotEmpty())
                    assertTrue(filterIsInstance<PaymentMethodHeader>().isNotEmpty())
                    assertTrue(filterIsInstance<GiftCardPaymentMethodModel>().isEmpty())
                    assertTrue(filterIsInstance<PaymentMethodNote>().isEmpty())
                }
            }
        }

        @Test
        fun `stored and normal payment methods lists are empty, then payment method flow won't contain stored or normal payment methods`() =
            runTest {
                viewModel = createViewModel(
                    storedPaymentMethods = emptyList(),
                    paymentMethods = emptyList(),
                )

                viewModel.paymentMethodsFlow.test {
                    with(expectMostRecentItem()) {
                        assertTrue(filterIsInstance<StoredPaymentMethodModel>().isEmpty())
                        assertTrue(filterIsInstance<PaymentMethodModel>().isEmpty())
                        assertTrue(filterIsInstance<PaymentMethodHeader>().isNotEmpty())
                        assertTrue(filterIsInstance<GiftCardPaymentMethodModel>().isNotEmpty())
                        assertTrue(filterIsInstance<PaymentMethodNote>().isNotEmpty())
                    }
                }
            }

        @Test
        fun `all payment methods are empty, then payment method flow will be empty`() = runTest {
            viewModel = createViewModel(
                storedPaymentMethods = emptyList(),
                paymentMethods = emptyList(),
                order = null,
            )

            viewModel.paymentMethodsFlow.test {
                with(expectMostRecentItem()) {
                    assertTrue(isEmpty())
                }
            }
        }
    }

    private fun createViewModel(
        checkoutConfiguration: CheckoutConfiguration = ConfigurationProvider.getCheckoutConfiguration(),
        amount: Amount = Amount(currency = "EUR", value = 1234567),
        paymentMethods: List<PaymentMethod> = DataProvider.getPaymentMethodsList(),
        storedPaymentMethods: List<StoredPaymentMethod> = DataProvider.getStoredPaymentMethods(),
        order: OrderModel? = DataProvider.getOrder(),
    ) = PaymentMethodsListViewModel(
        application = application,
        paymentMethods = paymentMethods,
        storedPaymentMethods = storedPaymentMethods,
        order = order,
        checkoutConfiguration = checkoutConfiguration,
        dropInParams = DropInParamsMapper()
            .mapToParams(checkoutConfiguration, Locale.US, null),
        dropInOverrideParams = DropInOverrideParams(amount, null),
    )
}
