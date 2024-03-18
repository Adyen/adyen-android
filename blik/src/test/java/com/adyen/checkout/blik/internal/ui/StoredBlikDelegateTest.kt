/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/3/2024.
 */

package com.adyen.checkout.blik.internal.ui

import com.adyen.checkout.blik.BlikComponentState
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.blik.blik
import com.adyen.checkout.blik.getBlikConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
class StoredBlikDelegateTest(
    @Mock private val analyticsManager: AnalyticsManager,
    @Mock private val submitHandler: SubmitHandler<BlikComponentState>,
) {

    private lateinit var delegate: StoredBlikDelegate

    @BeforeEach
    fun beforeEach() {
        delegate = createStoredBlikDelegate()
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when delegate is initialized then analytics manager is initialized`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            verify(analyticsManager).initialize(eq(delegate), any())
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()
            verify(analyticsManager).clear(eq(delegate))
        }
    }

    private fun createStoredBlikDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration()
    ) = StoredBlikDelegate(
        observerRepository = PaymentObserverRepository(),
        componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getBlikConfiguration(),
        ),
        storedPaymentMethod = StoredPaymentMethod(id = STORED_ID),
        order = TEST_ORDER,
        analyticsManager = analyticsManager,
        submitHandler = submitHandler,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: BlikConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        blik(configuration)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val STORED_ID = "Stored_id"
    }
}
