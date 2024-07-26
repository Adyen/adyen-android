package com.adyen.checkout.instant.internal.provider

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.instant.InstantPaymentComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class InstantPaymentComponentProviderTest {

    private lateinit var provider: InstantPaymentComponentProvider

    @BeforeEach
    fun setup() {
        provider = InstantPaymentComponentProvider()
    }

    @ParameterizedTest
    @MethodSource("supportedSource")
    fun `when payment method is, then should it be supported`(paymentMethodType: String, isSupported: Boolean) {
        val paymentMethod = PaymentMethod(type = paymentMethodType)

        val result = provider.isPaymentMethodSupported(paymentMethod)

        assertEquals(isSupported, result)
    }

    companion object {

        @JvmStatic
        fun supportedSource() =
            // Supported instant payment methods
            listOf(
                arguments("paypal", true),
                arguments("klarna", true),
            ) +
                // Only action only payment methods are supported
                PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.map {
                    val isSupported = InstantPaymentComponent.PAYMENT_METHOD_TYPES.contains(it)
                    arguments(it, isSupported)
                } +
                // Unsupported payment methods are not supported
                PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS.map {
                    arguments(it, false)
                }
    }
}
