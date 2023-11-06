/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/11/2023.
 */

package com.adyen.checkout.components.core.extension

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodCustomDisplayInformation
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.components.core.StoredPaymentMethod
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class PaymentMethodsApiResponseExtensionTest {

    private val customDisplayInformation = PaymentMethodCustomDisplayInformation("customName")

    @Test
    fun `When adding custom display information for a payment method by type, updates the correct payment methods`() {
        generatePaymentMethodsApiResponse().apply {
            addPaymentMethodCustomDisplayInformation(
                type = "testType1",
                customDisplayInformation = customDisplayInformation
            )

            paymentMethods?.filter { paymentMethod ->
                paymentMethod.type == "testType1"
            }?.forEach { paymentMethod ->
                assertEquals(
                    customDisplayInformation,
                    paymentMethod.customDisplayInformation
                )
            }
            paymentMethods?.filter { paymentMethod ->
                paymentMethod.type != "testType1"
            }?.forEach { paymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    paymentMethod.customDisplayInformation
                )
            }
            storedPaymentMethods?.forEach { storedPaymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    storedPaymentMethod.customDisplayInformation
                )
            }
        }
    }

    @Test
    fun `when adding custom display information for a payment method by type and predicate, updates the correct payment method`() {
        generatePaymentMethodsApiResponse().apply {
            addPaymentMethodCustomDisplayInformation(
                type = "testType1",
                customDisplayInformation = customDisplayInformation
            ) { paymentMethod ->
                paymentMethod.brand == "brand1"
            }

            paymentMethods?.filter { paymentMethod ->
                paymentMethod.type == "testType1" && paymentMethod.brand == "brand1"
            }?.forEach { paymentMethod ->
                assertEquals(
                    customDisplayInformation,
                    paymentMethod.customDisplayInformation
                )
            }
            paymentMethods?.filter { paymentMethod ->
                paymentMethod.type != "testType1" || paymentMethod.brand != "brand1"
            }?.forEach { paymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    paymentMethod.customDisplayInformation
                )
            }
            storedPaymentMethods?.forEach { storedPaymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    storedPaymentMethod.customDisplayInformation
                )
            }
        }
    }

    @Test
    fun `when adding custom display information for a payment method of non existing type, does not update any payment method`() {
        generatePaymentMethodsApiResponse().apply {
            addPaymentMethodCustomDisplayInformation(
                type = "nonExistingType",
                customDisplayInformation = customDisplayInformation
            )

            paymentMethods?.forEach { paymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    paymentMethod.customDisplayInformation
                )
            }
            storedPaymentMethods?.forEach { storedPaymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    storedPaymentMethod.customDisplayInformation
                )
            }
        }
    }

    @Test
    fun `when adding custom display information for a stored payment method by type, updates the correct stored payment methods`() {
        generatePaymentMethodsApiResponse().apply {
            addStoredPaymentMethodCustomDisplayInformation(
                type = "testType1",
                customDisplayInformation = customDisplayInformation
            )

            storedPaymentMethods?.filter { storedPaymentMethod ->
                storedPaymentMethod.type == "testType1"
            }?.forEach { storedPaymentMethod ->
                assertEquals(
                    customDisplayInformation,
                    storedPaymentMethod.customDisplayInformation
                )
            }
            storedPaymentMethods?.filter { storedPaymentMethod ->
                storedPaymentMethod.type != "testType1"
            }?.forEach { storedPaymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    storedPaymentMethod.customDisplayInformation
                )
            }
            paymentMethods?.forEach { paymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    paymentMethod.customDisplayInformation
                )
            }
        }
    }

    @Test
    fun `when adding custom display information for a stored payment method by type and predicate, updates the correct stored payment method`() {
        generatePaymentMethodsApiResponse().apply {
            addStoredPaymentMethodCustomDisplayInformation(
                type = "testType1",
                customDisplayInformation = customDisplayInformation
            ) { storedPaymentMethod ->
                storedPaymentMethod.brand == "brand1"
            }

            storedPaymentMethods?.filter { storedPaymentMethod ->
                storedPaymentMethod.type == "testType1" && storedPaymentMethod.brand == "brand1"
            }?.forEach { storedPaymentMethod ->
                assertEquals(
                    customDisplayInformation,
                    storedPaymentMethod.customDisplayInformation
                )
            }
            storedPaymentMethods?.filter { storedPaymentMethod ->
                storedPaymentMethod.type != "testType1" || storedPaymentMethod.brand != "brand1"
            }?.forEach { storedPaymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    storedPaymentMethod.customDisplayInformation
                )
            }
            paymentMethods?.forEach { paymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    paymentMethod.customDisplayInformation
                )
            }
        }
    }

    @Test
    fun `when adding custom display information for a stored payment method of non existing type, does not update any stored payment method`() {
        generatePaymentMethodsApiResponse().apply {
            addStoredPaymentMethodCustomDisplayInformation(
                type = "nonExistingType",
                customDisplayInformation = customDisplayInformation
            )

            storedPaymentMethods?.forEach { storedPaymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    storedPaymentMethod.customDisplayInformation
                )
            }
            paymentMethods?.forEach { paymentMethod ->
                assertNotEquals(
                    customDisplayInformation,
                    paymentMethod.customDisplayInformation
                )
            }
        }
    }

    private fun generatePaymentMethodsApiResponse() = PaymentMethodsApiResponse(
        paymentMethods = listOf(
            PaymentMethod(type = "testType1", name = "paymentMethod1", brand = "brand1"),
            PaymentMethod(type = "testType1", name = "paymentMethod2", brand = "brand2"),
            PaymentMethod(type = "testType2", name = "paymentMethod3", brand = "brand3"),
            PaymentMethod(type = "testType3", name = "paymentMethod4", brand = "brand4"),
        ),
        storedPaymentMethods = listOf(
            StoredPaymentMethod(type = "testType1", name = "savedPaymentMethod1", brand = "brand1"),
            StoredPaymentMethod(type = "testType1", name = "savedPaymentMethod2", brand = "brand2"),
            StoredPaymentMethod(type = "testType2", name = "savedPaymentMethod3", brand = "brand3"),
            StoredPaymentMethod(type = "testType3", name = "savedPaymentMethod4", brand = "brand4"),
        )
    )
}
