/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.TestPaymentMethodRepository
import com.adyen.checkout.test.LoggingExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Locale

@ExtendWith(LoggingExtension::class)
internal class PreselectedPaymentMethodViewModelTest {

    private lateinit var navigator: DropInNavigator

    private val dropInParams = DropInParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        amount = Amount(currency = "USD", value = 999L),
    )

    private val storedPaymentMethod = StoredCardPaymentMethod(
        type = PaymentMethodTypes.SCHEME,
        name = "Visa",
        id = "stored-id-1",
        supportedShopperInteractions = listOf("Ecommerce"),
        brand = "visa",
        lastFour = "1234",
        expiryMonth = "01",
        expiryYear = "2030",
        holderName = null,
        fundingSource = null,
    )

    @BeforeEach
    fun setUp() {
        navigator = DropInNavigator()
    }

    @Test
    fun `when stored payment method is found, then view state is populated`() {
        val repository = TestPaymentMethodRepository(listOf(storedPaymentMethod))

        val viewModel = createViewModel(repository, storedPaymentMethodId = "stored-id-1")

        assertNotNull(viewModel.viewState.value)
    }

    @Test
    fun `when stored payment method is found, then logo tx variant is set from formatter`() {
        val repository = TestPaymentMethodRepository(listOf(storedPaymentMethod))

        val viewModel = createViewModel(repository, storedPaymentMethodId = "stored-id-1")

        assertEquals("visa", viewModel.viewState.value?.logoTxVariant)
    }

    @Test
    fun `when stored payment method is found, then title is set from formatter`() {
        val repository = TestPaymentMethodRepository(listOf(storedPaymentMethod))

        val viewModel = createViewModel(repository, storedPaymentMethodId = "stored-id-1")

        assertEquals("•••• 1234", viewModel.viewState.value?.title)
    }

    @Test
    fun `when stored payment method is not found, then view state is null`() {
        val repository = TestPaymentMethodRepository(listOf(storedPaymentMethod))

        val viewModel = createViewModel(repository, storedPaymentMethodId = "unknown-id")

        assertNull(viewModel.viewState.value)
    }

    @Test
    fun `when stored payment method is not found, then navigates to payment method list`() {
        val repository = TestPaymentMethodRepository(listOf(storedPaymentMethod))

        createViewModel(repository, storedPaymentMethodId = "unknown-id")

        assertEquals(listOf(EmptyNavKey, PaymentMethodListNavKey), navigator.backStack)
    }

    @Test
    fun `when onBackClicked, then navigator goes back`() {
        navigator.navigateTo(PaymentMethodListNavKey)
        val repository = TestPaymentMethodRepository(listOf(storedPaymentMethod))
        val viewModel = createViewModel(repository, storedPaymentMethodId = "stored-id-1")

        viewModel.onBackClicked()

        assertEquals(listOf(EmptyNavKey), navigator.backStack)
    }

    @Test
    fun `when onPayClicked, then navigates to payment method with stored type`() {
        val repository = TestPaymentMethodRepository(listOf(storedPaymentMethod))
        val viewModel = createViewModel(repository, storedPaymentMethodId = "stored-id-1")

        viewModel.onPayClicked()

        val expectedKey = PaymentMethodNavKey(DropInPaymentFlowType.StoredPaymentMethod("stored-id-1"))
        assertEquals(listOf(EmptyNavKey, expectedKey), navigator.backStack)
    }

    @Test
    fun `when onOtherPaymentMethodClicked, then navigates to payment method list`() {
        val repository = TestPaymentMethodRepository(listOf(storedPaymentMethod))
        val viewModel = createViewModel(repository, storedPaymentMethodId = "stored-id-1")

        viewModel.onOtherPaymentMethodClicked()

        assertEquals(listOf(EmptyNavKey, PaymentMethodListNavKey), navigator.backStack)
    }

    private fun createViewModel(
        repository: TestPaymentMethodRepository,
        storedPaymentMethodId: String,
    ) = PreselectedPaymentMethodViewModel(
        dropInParams = dropInParams,
        paymentMethodRepository = repository,
        storedPaymentMethodId = storedPaymentMethodId,
        navigator = navigator,
    )
}
