/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 27/10/2022.
 */

package com.adyen.checkout.internal.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.internal.ui.PaymentMethodsListViewModel
import com.adyen.checkout.dropin.internal.ui.model.GiftCardPaymentMethodModel
import com.adyen.checkout.dropin.internal.ui.model.OrderModel
import com.adyen.checkout.dropin.internal.ui.model.PaymentMethodHeader
import com.adyen.checkout.dropin.internal.ui.model.PaymentMethodModel
import com.adyen.checkout.dropin.internal.ui.model.PaymentMethodNote
import com.adyen.checkout.dropin.internal.ui.model.StoredPaymentMethodModel
import com.adyen.checkout.internal.ConfigurationProvider
import com.adyen.checkout.internal.DataProvider
import com.adyen.checkout.internal.Helpers.mapToPaymentMethodModelList
import com.adyen.checkout.internal.Helpers.mapToStoredPaymentMethodsModelList
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
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

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class PaymentMethodsListViewModelTest(
    @Mock private val application: Application
) {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var configuration: DropInConfiguration
    private lateinit var amount: Amount
    private lateinit var paymentMethods: List<PaymentMethod>
    private lateinit var storedPaymentMethods: MutableList<StoredPaymentMethod>
    private lateinit var viewModel: PaymentMethodsListViewModel
    private var order: OrderModel? = null

    @BeforeEach
    fun setup() {
        configuration = ConfigurationProvider.getDropInConfiguration()
        amount = Amount(currency = "EUR", value = 1234567)
        paymentMethods = DataProvider.getPaymentMethodsList()
        storedPaymentMethods = DataProvider.getStoredPaymentMethods().toMutableList()
        order = DataProvider.getOrder()
        whenever(application.getString(any(), any())) doReturn "string"
        viewModel = PaymentMethodsListViewModel(
            application = application,
            paymentMethods = paymentMethods,
            storedPaymentMethods = storedPaymentMethods,
            order = order,
            dropInConfiguration = configuration,
            amount = amount
        )
    }

    @Test
    fun `test remove stored payment method success`() = runTest {
        viewModel.paymentMethodsFlow.test {
            val paymentMethods = storedPaymentMethods
                .mapToStoredPaymentMethodsModelList(configuration.isRemovingStoredPaymentMethodsEnabled)
            val storedPaymentMethod = paymentMethods[0]

            viewModel.removePaymentMethodWithId(storedPaymentMethod.id)

            with(expectMostRecentItem()) {
                assertFalse(contains(storedPaymentMethod))
            }
        }
    }

    @Test
    fun `test get payment method from payment method model success`() {
        val paymentMethodModelList = paymentMethods.mapToPaymentMethodModelList()

        val paymentMethod = viewModel.getPaymentMethod(paymentMethodModelList[0])

        assertEquals(paymentMethod, paymentMethods[0])
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
            paymentMethods = emptyList()

            viewModel = PaymentMethodsListViewModel(
                application = application,
                paymentMethods = paymentMethods,
                storedPaymentMethods = storedPaymentMethods,
                order = order,
                dropInConfiguration = configuration,
                amount = amount
            )

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
                storedPaymentMethods = mutableListOf()

                viewModel = PaymentMethodsListViewModel(
                    application = application,
                    paymentMethods = paymentMethods,
                    storedPaymentMethods = storedPaymentMethods,
                    order = order,
                    dropInConfiguration = configuration,
                    amount = amount
                )

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
        fun `order is null, then payment method flow won't contain gift cards or payment method note`() =
            runTest {
                order = null

                viewModel = PaymentMethodsListViewModel(
                    application = application,
                    paymentMethods = paymentMethods,
                    storedPaymentMethods = storedPaymentMethods,
                    order = order,
                    dropInConfiguration = configuration,
                    amount = amount
                )

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
                storedPaymentMethods = mutableListOf()
                paymentMethods = emptyList()

                viewModel = PaymentMethodsListViewModel(
                    application = application,
                    paymentMethods = paymentMethods,
                    storedPaymentMethods = storedPaymentMethods,
                    order = order,
                    dropInConfiguration = configuration,
                    amount = amount
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
        fun `all payment methods are empty, then payment method flow will be empty`() =
            runTest {
                storedPaymentMethods = mutableListOf()
                paymentMethods = emptyList()
                order = null

                viewModel = PaymentMethodsListViewModel(
                    application = application,
                    paymentMethods = paymentMethods,
                    storedPaymentMethods = storedPaymentMethods,
                    order = order,
                    dropInConfiguration = configuration,
                    amount = amount
                )

                viewModel.paymentMethodsFlow.test {
                    with(expectMostRecentItem()) {
                        assertTrue(isEmpty())
                    }
                }
            }
    }
}
