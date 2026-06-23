/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/6/2026.
 */

package com.adyen.checkout.core.components.internal

import android.os.Parcel
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredBLIKPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.data.provider.TestSdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.TestPaymentComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

internal class PaymentComponentResolverTest {

    @BeforeEach
    fun setUp() {
        PaymentMethodProvider.clear()
    }

    @Test
    fun `when payment method is not supported, then Failure is returned`() = runTest {
        val paymentMethod = CardPaymentMethod(
            type = "scheme",
            name = "Card",
            brands = emptyList(),
            fundingSource = null,
        )
        val result = resolve(paymentMethod)

        assertEquals(
            PaymentComponentResult.Failure(
                "Payment method 'scheme' is not supported. " +
                    "Ensure the corresponding module is included in your build dependencies.",
            ),
            result,
        )
    }

    @Test
    fun `when stored payment method is not supported, then Failure is returned`() = runTest {
        val result = resolve(
            StoredBLIKPaymentMethod(
                type = "blik",
                name = "BLIK",
                id = "test_id",
                supportedShopperInteractions = emptyList(),
            ),
        )

        assertEquals(
            PaymentComponentResult.Failure(
                "Stored payment method type 'blik' is not supported. " +
                    "Ensure the corresponding module is included in your build dependencies.",
            ),
            result,
        )
    }

    @Test
    fun `when payment method response type is unsupported, then Failure is returned`() = runTest {
        val result = resolve(
            object : PaymentMethodResponse() {
                override val type: String = "unknown"
                override val name: String = "Unknown"
                override fun writeToParcel(dest: Parcel, flags: Int) = Unit
            },
        )

        assertEquals(
            PaymentComponentResult.Failure("Unsupported payment method response type."),
            result,
        )
    }

    @Test
    fun `when payment method is supported, then Success with the component is returned`() = runTest {
        val component = TestPaymentComponent()
        registerFactory(type = "scheme", component = component)

        val result = resolve(GenericPaymentMethod(type = "scheme", name = "Card"))

        assertEquals(PaymentComponentResult.Success(component), result)
    }

    @Test
    fun `when stored payment method is supported, then Success with the component is returned`() = runTest {
        val component = TestPaymentComponent()
        registerStoredFactory(type = "blik", component = component)

        val result = resolve(
            StoredBLIKPaymentMethod(
                type = "blik",
                name = "BLIK",
                id = "test_id",
                supportedShopperInteractions = emptyList(),
            ),
        )

        assertEquals(PaymentComponentResult.Success(component), result)
    }

    private fun CoroutineScope.resolve(
        paymentMethod: PaymentMethodResponse,
    ): PaymentComponentResult = PaymentComponentResolver.resolve(
        paymentMethod = paymentMethod,
        callbacks = advancedCallbacks(),
        coroutineScope = this,
        analyticsManager = TestAnalyticsManager(),
        sdkDataProvider = TestSdkDataProvider(),
        checkoutParams = checkoutParams(),
    )

    private fun advancedCallbacks() = AdvancedCheckoutCallbacks(
        onSubmit = { SubmitResult.Completion("") },
        onAdditionalDetails = { AdditionalDetailsResult.Completion("") },
        onFailure = {},
    )

    private fun checkoutParams() = CheckoutParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = true,
        publicKey = "test_publicKey",
        additionalConfigurations = emptyMap(),
        additionalSessionParams = null,
    )

    private fun registerFactory(type: String, component: PaymentComponent) {
        PaymentMethodProvider.register(
            type,
            object : PaymentComponentFactory<PaymentComponent> {
                override fun create(
                    paymentMethod: PaymentMethod,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    sdkDataProvider: SdkDataProvider,
                    params: CheckoutParams,
                    additionalCallbacks: Set<CheckoutAdditionalCallback>,
                ): PaymentComponent = component
            },
        )
    }

    private fun registerStoredFactory(type: String, component: PaymentComponent) {
        PaymentMethodProvider.register(
            type,
            object : StoredPaymentComponentFactory<PaymentComponent> {
                override fun create(
                    storedPaymentMethod: StoredPaymentMethod,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    sdkDataProvider: SdkDataProvider,
                    params: CheckoutParams,
                ): PaymentComponent = component
            },
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"
    }
}
