/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.mutableStateListOf
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.TestCheckoutContext
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.DropInService
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.dropin.internal.helper.BackStackPersister
import com.adyen.checkout.dropin.internal.helper.InMemoryBackStackPersister
import com.adyen.checkout.dropin.internal.service.DropInServiceManager
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class, TestDispatcherExtension::class)
internal class DropInViewModelTest {

    private val controllerProvider = DropInControllerProvider { _, _ -> mock<CheckoutController>() }

    @Test
    fun `when there are no stored payment methods, then the payment method list is the starting point`() {
        val viewModel = createViewModel(storedPaymentMethods = emptyList())

        assertEquals(listOf(EmptyNavKey, PaymentMethodListNavKey), viewModel.navigator.backStack)
    }

    @Test
    fun `when there are stored payment methods, then the first one is preselected`() {
        val viewModel = createViewModel(storedPaymentMethods = listOf(storedPaymentMethod(STORED_ID)))

        assertEquals(
            listOf(EmptyNavKey, PreselectedPaymentMethodNavKey(STORED_ID)),
            viewModel.navigator.backStack,
        )
    }

    @Test
    fun `when the back stack was restored, then no starting point is added`() {
        // A persister that already holds a back stack stands in for a recreated Drop-in.
        val persister = InMemoryBackStackPersister()
        persister.store(mutableStateListOf(EmptyNavKey, StoredPaymentMethodsNavKey))

        val viewModel = createViewModel(storedPaymentMethods = emptyList(), persister = persister)

        assertEquals(listOf(EmptyNavKey, StoredPaymentMethodsNavKey), viewModel.navigator.backStack)
    }

    @Test
    fun `when created, then the injected controller provider is exposed`() {
        val viewModel = createViewModel()

        // The payment method entries resolve their controllers through this instance.
        assertSame(controllerProvider, viewModel.controllerProvider)
    }

    @Test
    fun `when created, then the drop in params are mapped from the checkout configuration`() {
        val viewModel = createViewModel()

        assertEquals(Locale.US, viewModel.dropInParams.shopperLocale)
        assertEquals(Environment.TEST, viewModel.dropInParams.environment)
        assertEquals(AMOUNT, viewModel.dropInParams.amount)
    }

    private fun createViewModel(
        storedPaymentMethods: List<StoredPaymentMethod> = emptyList(),
        paymentMethods: List<PaymentMethod> = listOf(
            GenericPaymentMethod(type = PaymentMethodTypes.SCHEME, name = "Cards"),
        ),
        persister: BackStackPersister = InMemoryBackStackPersister(),
    ): DropInViewModel {
        val checkoutContext = TestCheckoutContext.advanced(
            paymentMethods = PaymentMethods(
                storedPaymentMethods = storedPaymentMethods,
                paymentMethods = paymentMethods,
            ),
            checkoutConfiguration = CheckoutConfiguration(
                environment = Environment.TEST,
                clientKey = CLIENT_KEY,
                shopperLocale = Locale.US,
                amount = AMOUNT,
            ),
        )

        return DropInViewModel(
            input = DropInResultContract.Input(
                checkoutContext = checkoutContext,
                serviceClass = DropInService::class.java,
                theme = CheckoutTheme(),
            ),
            navigator = DropInNavigator(persister),
            controllerProvider = controllerProvider,
            dropInServiceManager = DropInServiceManager(DropInService::class.java),
        )
    }

    private fun storedPaymentMethod(id: String) = StoredCardPaymentMethod(
        type = PaymentMethodTypes.SCHEME,
        name = "Visa",
        id = id,
        supportedShopperInteractions = listOf("Ecommerce"),
        brand = "visa",
        lastFour = "1234",
        expiryMonth = "01",
        expiryYear = "2030",
        holderName = null,
        fundingSource = null,
    )

    private companion object {
        private const val STORED_ID = "stored-id-1"
        private const val CLIENT_KEY = "test_client_key"
        private val AMOUNT = Amount(currency = "USD", value = 999L)
    }
}
