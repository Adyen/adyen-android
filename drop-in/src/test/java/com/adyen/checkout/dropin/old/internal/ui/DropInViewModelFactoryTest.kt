/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.ui

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.dropin.old.internal.ui.model.DropInPaymentMethodInformation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.mockito.kotlin.mock

internal class DropInViewModelFactoryTest {

    @Test
    fun `when overriding payment information for a payment method by type, updates the correct payment methods`() {
        val bundleHandler = DropInSavedStateHandleContainer(mock()).apply {
            paymentMethodsApiResponse = generatePaymentMethodsApiResponse()
        }
        val paymentMethodInformationMap = hashMapOf(
            Pair("testType1", DropInPaymentMethodInformation("custom payment method"))
        )

        bundleHandler.overridePaymentMethodInformation(paymentMethodInformationMap)

        bundleHandler.paymentMethodsApiResponse?.apply {
            paymentMethods?.filter { paymentMethod ->
                paymentMethod.type == "testType1"
            }?.forEach { paymentMethod ->
                assertEquals(
                    "custom payment method",
                    paymentMethod.name
                )
            }
            paymentMethods?.filter { paymentMethod ->
                paymentMethod.type != "testType1"
            }?.forEach { paymentMethod ->
                assertNotEquals(
                    "custom payment method",
                    paymentMethod.name
                )
            }
            storedPaymentMethods?.forEach { storedPaymentMethod ->
                assertNotEquals(
                    "custom payment method",
                    storedPaymentMethod.name
                )
            }
        }
    }

    @Test
    fun `when overriding payment information for a payment method by non existing type, does not update any payment method`() {
        val bundleHandler = DropInSavedStateHandleContainer(mock()).apply {
            paymentMethodsApiResponse = generatePaymentMethodsApiResponse()
        }
        val paymentMethodInformationMap = hashMapOf(
            Pair("nonExistingType", DropInPaymentMethodInformation("custom payment method"))
        )

        bundleHandler.overridePaymentMethodInformation(paymentMethodInformationMap)

        bundleHandler.paymentMethodsApiResponse?.apply {
            paymentMethods?.forEach { paymentMethod ->
                assertNotEquals(
                    "custom payment method",
                    paymentMethod.name
                )
            }
            storedPaymentMethods?.forEach { storedPaymentMethod ->
                assertNotEquals(
                    "custom payment method",
                    storedPaymentMethod.name
                )
            }
        }
    }

    private fun generatePaymentMethodsApiResponse() = PaymentMethodsApiResponse(
        paymentMethods = listOf(
            PaymentMethod(type = "testType1", name = "paymentMethod1"),
            PaymentMethod(type = "testType1", name = "paymentMethod2"),
            PaymentMethod(type = "testType2", name = "paymentMethod3"),
            PaymentMethod(type = "testType3", name = "paymentMethod4"),
        ),
        storedPaymentMethods = listOf(
            StoredPaymentMethod(type = "testType1", name = "savedPaymentMethod1"),
            StoredPaymentMethod(type = "testType1", name = "savedPaymentMethod2"),
            StoredPaymentMethod(type = "testType2", name = "savedPaymentMethod3"),
            StoredPaymentMethod(type = "testType3", name = "savedPaymentMethod4"),
        )
    )
}
