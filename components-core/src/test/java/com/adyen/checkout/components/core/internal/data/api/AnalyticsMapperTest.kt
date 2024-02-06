/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/11/2022.
 */

package com.adyen.checkout.components.core.internal.data.api

import android.os.Build
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@ExtendWith(MockitoExtension::class)
internal class AnalyticsMapperTest {

//    private val analyticsMapper: AnalyticsMapper = AnalyticsMapper()
//
//    @BeforeEach
//    fun beforeEach() {
//        AnalyticsMapper.resetToDefaults()
//    }
//
//    @Nested
//    @DisplayName("when getFlavorQueryParameter is called and")
//    inner class GetFlavorQueryParameterTest {
//
//        @Test
//        fun `source is drop-in then returned value is dropin`() {
//            val actual = analyticsMapper.getFlavorQueryParameter(AnalyticsSource.DropIn)
//            assertEquals("dropin", actual)
//        }
//
//        @Test
//        fun `source is a component created from drop-in then returned value is dropin`() {
//            val actual = analyticsMapper.getFlavorQueryParameter(
//                AnalyticsSource.PaymentComponent(
//                    isCreatedByDropIn = true,
//                    paymentMethod = PaymentMethod(),
//                )
//            )
//            assertEquals("dropin", actual)
//        }
//
//        @Test
//        fun `source is a component not created from drop-in then returned value is components`() {
//            val actual = analyticsMapper.getFlavorQueryParameter(
//                AnalyticsSource.PaymentComponent(
//                    isCreatedByDropIn = false,
//                    paymentMethod = PaymentMethod(),
//                )
//            )
//            assertEquals("components", actual)
//        }
//    }
//
//    @Nested
//    @DisplayName("when getComponentQueryParameter is called and")
//    inner class GetComponentQueryParameterTest {
//
//        @Test
//        fun `source is drop-in then returned value is dropin`() {
//            val actual = analyticsMapper.getComponentQueryParameter(AnalyticsSource.DropIn)
//            assertEquals("dropin", actual)
//        }
//
//        @Test
//        fun `source is a component with a payment method then returned value is the payment method type`() {
//            val actual = analyticsMapper.getComponentQueryParameter(
//                AnalyticsSource.PaymentComponent(
//                    isCreatedByDropIn = true,
//                    paymentMethod = PaymentMethod(type = "PAYMENT_METHOD_TYPE"),
//                )
//            )
//            assertEquals("PAYMENT_METHOD_TYPE", actual)
//        }
//
//        @Test
//        fun `source is a component with a stored payment method then returned value is the stored payment method type`() {
//            val actual = analyticsMapper.getComponentQueryParameter(
//                AnalyticsSource.PaymentComponent(
//                    isCreatedByDropIn = false,
//                    storedPaymentMethod = StoredPaymentMethod(type = "STORED_PAYMENT_METHOD_TYPE"),
//                )
//            )
//            assertEquals("STORED_PAYMENT_METHOD_TYPE", actual)
//        }
//    }
//
//    @Nested
//    @DisplayName("when getQueryParameters is called")
//    inner class GetQueryParametersTest {
//
//        @Test
//        fun `then returned values should match expected`() {
//            val actual = analyticsMapper.getAnalyticsSetupRequest(
//                packageName = "PACKAGE_NAME",
//                locale = Locale("en", "US"),
//                source = AnalyticsSource.PaymentComponent(
//                    isCreatedByDropIn = false,
//                    PaymentMethod(type = "PAYMENT_METHOD_TYPE")
//                ),
//                amount = Amount("USD", 1337),
//                screenWidth = 1286,
//                paymentMethods = listOf("scheme", "googlepay"),
//                sessionId = "SESSION_ID",
//            )
//
//            val expected = AnalyticsSetupRequest(
//                version = "5.3.1",
//                channel = "android",
//                platform = "android",
//                locale = "en_US",
//                component = "PAYMENT_METHOD_TYPE",
//                flavor = "components",
//                deviceBrand = "null",
//                deviceModel = "null",
//                referrer = "PACKAGE_NAME",
//                systemVersion = Build.VERSION.SDK_INT.toString(),
//                containerWidth = null,
//                screenWidth = 1286,
//                paymentMethods = listOf("scheme", "googlepay"),
//                amount = Amount("USD", 1337),
//                sessionId = "SESSION_ID",
//            )
//
//            assertEquals(expected.toString(), actual.toString())
//        }
//    }
//
//    @Test
//    fun `when cross platform parameters are overridden, then returned values should match expected`() {
//        AnalyticsMapper.overrideForCrossPlatform(AnalyticsPlatform.FLUTTER, "some test version")
//        val actual = analyticsMapper.getAnalyticsSetupRequest(
//            packageName = "PACKAGE_NAME",
//            locale = Locale("en", "US"),
//            source = AnalyticsSource.PaymentComponent(
//                isCreatedByDropIn = false,
//                PaymentMethod(type = "PAYMENT_METHOD_TYPE")
//            ),
//            amount = Amount("USD", 1337),
//            screenWidth = 1286,
//            paymentMethods = listOf("scheme", "googlepay"),
//            sessionId = "SESSION_ID",
//        )
//
//        val expected = AnalyticsSetupRequest(
//            version = "some test version",
//            channel = "android",
//            platform = "flutter",
//            locale = "en_US",
//            component = "PAYMENT_METHOD_TYPE",
//            flavor = "components",
//            deviceBrand = "null",
//            deviceModel = "null",
//            referrer = "PACKAGE_NAME",
//            systemVersion = Build.VERSION.SDK_INT.toString(),
//            containerWidth = null,
//            screenWidth = 1286,
//            paymentMethods = listOf("scheme", "googlepay"),
//            amount = Amount("USD", 1337),
//            sessionId = "SESSION_ID",
//        )
//
//        assertEquals(expected.toString(), actual.toString())
//    }
}
