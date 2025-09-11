/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/12/2023.
 */

package com.adyen.checkout.googlepay.internal.util

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.util.CheckoutPlatform
import com.adyen.checkout.components.core.internal.util.CheckoutPlatformParams
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.googlepay.BillingAddressParameters
import com.adyen.checkout.googlepay.BuildConfig
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.ShippingAddressParameters
import com.adyen.checkout.googlepay.SoftwareInfo
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONObject
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class GooglePayUtilsTest {

    @Test
    fun `when creating IsReadyToPayRequest with default or empty GooglePayComponentParams then results match`() {
        val isReadyToPayRequest = GooglePayUtils.createIsReadyToPayRequest(getEmptyGooglePayComponentParams())
        val expectedSerializedIsReadyToPayRequest = JSONObject(
            """
                {
                    "apiVersionMinor": 0,
                    "apiVersion": 2,
                    "allowedPaymentMethods":
                    [
                        {
                            "type": "CARD",
                            "parameters":
                            {
                                "allowedAuthMethods": [],
                                "billingAddressRequired": false,
                                "allowedCardNetworks": [],
                                "allowPrepaidCards": false
                            },
                            "tokenizationSpecification":
                            {
                                "type": "PAYMENT_GATEWAY",
                                "parameters":
                                {
                                    "gatewayMerchantId": "",
                                    "gateway": "adyen"
                                }
                            }
                        }
                    ],
                    "existingPaymentMethodRequired": false
                }
            """.trimIndent(),
        ).toString()

        assertEquals(expectedSerializedIsReadyToPayRequest, isReadyToPayRequest.toJson())
    }

    @Test
    fun `when creating IsReadyToPayRequest with custom GooglePayComponentParams then results match`() {
        val isReadyToPayRequest = GooglePayUtils.createIsReadyToPayRequest(getCustomGooglePayComponentParams())
        val expectedSerializedIsReadyToPayRequest = JSONObject(
            """
                {
                    "apiVersionMinor": 0,
                    "apiVersion": 2,
                    "allowedPaymentMethods":
                    [
                        {
                            "type": "CARD",
                            "parameters":
                            {
                                "assuranceDetailsRequired": true,
                                "allowedAuthMethods":
                                [
                                    "AUTH_METHOD_1",
                                    "AUTH_METHOD_2"
                                ],
                                "billingAddressRequired": true,
                                "billingAddressParameters":
                                {
                                    "format": "FORMAT",
                                    "phoneNumberRequired": true
                                },
                                "allowedCardNetworks":
                                [
                                    "CARD_NETWORK_1",
                                    "CARD_NETWORK_2",
                                    "CARD_NETWORK_3"
                                ],
                                "allowCreditCards": true,
                                "allowPrepaidCards": true
                            },
                            "tokenizationSpecification":
                            {
                                "type": "PAYMENT_GATEWAY",
                                "parameters":
                                {
                                    "gatewayMerchantId": "GATEWAY_MERCHANT_ID",
                                    "gateway": "adyen"
                                }
                            }
                        }
                    ],
                    "existingPaymentMethodRequired": true
                }
            """.trimIndent(),
        ).toString()

        assertEquals(expectedSerializedIsReadyToPayRequest, isReadyToPayRequest.toJson())
    }

    @Test
    fun `when creating PaymentDataRequest with default or empty GooglePayComponentParams then results match`() {
        val paymentDataRequest = GooglePayUtils.createPaymentDataRequest(getEmptyGooglePayComponentParams())
        val expectedSerializedPaymentDataRequest = JSONObject(
            """
                {
                    "apiVersionMinor": 0,
                    "apiVersion": 2,
                    "merchantInfo":
                    {
                        "softwareInfo":
                        {
                            "id": "android/adyen-components",
                            "version": "develop"
                        }
                    },
                    "allowedPaymentMethods":
                    [
                        {
                            "type": "CARD",
                            "parameters":
                            {
                                "allowedAuthMethods": [],
                                "billingAddressRequired": false,
                                "allowedCardNetworks": [],
                                "allowPrepaidCards": false
                            },
                            "tokenizationSpecification":
                            {
                                "type": "PAYMENT_GATEWAY",
                                "parameters":
                                {
                                    "gatewayMerchantId": "",
                                    "gateway": "adyen"
                                }
                            }
                        }
                    ],
                    "shippingAddressRequired": false,
                    "emailRequired": false,
                    "transactionInfo":
                    {
                        "totalPriceStatus": "NOT_CURRENTLY_KNOWN",
                        "currencyCode": "USD"
                    }
                }
            """.trimIndent(),
        ).toString()

        assertEquals(expectedSerializedPaymentDataRequest, paymentDataRequest.toJson())
    }

    @Test
    fun `when creating PaymentDataRequest with custom GooglePayComponentParams then results match`() {
        val paymentDataRequest = GooglePayUtils.createPaymentDataRequest(getCustomGooglePayComponentParams())
        val expectedSerializedPaymentDataRequest = JSONObject(
            """
                {
                    "apiVersionMinor": 0,
                    "apiVersion": 2,
                    "merchantInfo":
                    {
                        "merchantId": "MERCHANT_ID",
                        "merchantName": "MERCHANT_NAME",
                        "softwareInfo":
                        {
                            "id": "android/adyen-dropin",
                            "version": "develop"
                        }
                    },
                    "allowedPaymentMethods":
                    [
                        {
                            "type": "CARD",
                            "parameters":
                            {
                                "assuranceDetailsRequired": true,
                                "allowedAuthMethods":
                                [
                                    "AUTH_METHOD_1",
                                    "AUTH_METHOD_2"
                                ],
                                "billingAddressRequired": true,
                                "billingAddressParameters":
                                {
                                    "format": "FORMAT",
                                    "phoneNumberRequired": true
                                },
                                "allowedCardNetworks":
                                [
                                    "CARD_NETWORK_1",
                                    "CARD_NETWORK_2",
                                    "CARD_NETWORK_3"
                                ],
                                "allowCreditCards": true,
                                "allowPrepaidCards": true
                            },
                            "tokenizationSpecification":
                            {
                                "type": "PAYMENT_GATEWAY",
                                "parameters":
                                {
                                    "gatewayMerchantId": "GATEWAY_MERCHANT_ID",
                                    "gateway": "adyen"
                                }
                            }
                        }
                    ],
                    "shippingAddressRequired": true,
                    "shippingAddressParameters":
                    {
                        "allowedCountryCodes":
                        [
                            "COUNTRY_1",
                            "COUNTRY_2"
                        ],
                        "phoneNumberRequired": true
                    },
                    "emailRequired": true,
                    "transactionInfo":
                    {
                        "totalPrice": "13.37",
                        "countryCode": "COUNTRY_CODE",
                        "totalPriceStatus": "TOTAL_PRICE_STATUS",
                        "currencyCode": "EUR",
                        "checkoutOption": "DEFAULT"
                    }
                }
            """.trimIndent(),
        ).toString()

        assertEquals(expectedSerializedPaymentDataRequest, paymentDataRequest.toJson())
    }

    @ParameterizedTest
    @MethodSource("softwareInfoSource")
    fun `when creating payment data request, then software info is added correctly`(
        platform: CheckoutPlatform,
        version: String?,
        isDropIn: Boolean,
        expectedId: String,
        expectedVersion: String,
    ) {
        CheckoutPlatformParams.resetDefaults()

        if (platform != CheckoutPlatform.ANDROID) {
            CheckoutPlatformParams.overrideForCrossPlatform(platform, requireNotNull(version))
        }

        val paymentDataRequest =
            GooglePayUtils.createPaymentDataRequest(getCustomGooglePayComponentParams(isCreatedByDropIn = isDropIn))
        val softwareInfoJson = JSONObject(paymentDataRequest.toJson())
            .getJSONObject("merchantInfo")
            .getJSONObject("softwareInfo")
        val softwareInfo = SoftwareInfo.SERIALIZER.deserialize(softwareInfoJson)

        assertEquals(expectedId, softwareInfo.id)
        assertEquals(expectedVersion, softwareInfo.version)
    }

    private fun getEmptyGooglePayComponentParams(): GooglePayComponentParams {
        return GooglePayComponentParams(
            commonComponentParams = CommonComponentParams(
                shopperLocale = Locale.US,
                environment = Environment.TEST,
                clientKey = "CLIENT_KEY",
                analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, "CLIENT_KEY"),
                isCreatedByDropIn = false,
                amount = null,
            ),
            amount = Amount("USD", 0),
            isSubmitButtonVisible = false,
            gatewayMerchantId = "",
            googlePayEnvironment = WalletConstants.ENVIRONMENT_TEST,
            totalPriceStatus = "NOT_CURRENTLY_KNOWN",
            countryCode = null,
            merchantInfo = null,
            allowedAuthMethods = emptyList(),
            allowedCardNetworks = emptyList(),
            isAllowPrepaidCards = false,
            isAllowCreditCards = null,
            isAssuranceDetailsRequired = null,
            isEmailRequired = false,
            isExistingPaymentMethodRequired = false,
            isShippingAddressRequired = false,
            shippingAddressParameters = null,
            isBillingAddressRequired = false,
            billingAddressParameters = null,
            checkoutOption = null,
            googlePayButtonStyling = null,
        )
    }

    private fun getCustomGooglePayComponentParams(isCreatedByDropIn: Boolean = true): GooglePayComponentParams {
        return GooglePayComponentParams(
            commonComponentParams = CommonComponentParams(
                shopperLocale = Locale.GERMAN,
                environment = Environment.EUROPE,
                clientKey = "CLIENT_KEY_CUSTOM",
                analyticsParams = AnalyticsParams(AnalyticsParamsLevel.INITIAL, "CLIENT_KEY_CUSTOM"),
                isCreatedByDropIn = isCreatedByDropIn,
                amount = Amount("EUR", 13_37),
            ),
            amount = Amount("EUR", 13_37),
            isSubmitButtonVisible = true,
            gatewayMerchantId = "GATEWAY_MERCHANT_ID",
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
            totalPriceStatus = "TOTAL_PRICE_STATUS",
            countryCode = "COUNTRY_CODE",
            merchantInfo = MerchantInfo(merchantName = "MERCHANT_NAME", merchantId = "MERCHANT_ID"),
            allowedAuthMethods = listOf("AUTH_METHOD_1", "AUTH_METHOD_2"),
            allowedCardNetworks = listOf("CARD_NETWORK_1", "CARD_NETWORK_2", "CARD_NETWORK_3"),
            isAllowPrepaidCards = true,
            isAllowCreditCards = true,
            isAssuranceDetailsRequired = true,
            isEmailRequired = true,
            isExistingPaymentMethodRequired = true,
            isShippingAddressRequired = true,
            shippingAddressParameters = ShippingAddressParameters(
                allowedCountryCodes = listOf(
                    "COUNTRY_1",
                    "COUNTRY_2",
                ),
                isPhoneNumberRequired = true,
            ),
            isBillingAddressRequired = true,
            billingAddressParameters = BillingAddressParameters(
                format = "FORMAT",
                isPhoneNumberRequired = true,
            ),
            checkoutOption = "DEFAULT",
            googlePayButtonStyling = null,
        )
    }

    companion object {

        @JvmStatic
        fun softwareInfoSource() = listOf(
            // platform, version, isDropIn, expectedId, expectedVersion
            arguments(CheckoutPlatform.ANDROID, null, true, "android/adyen-dropin", BuildConfig.CHECKOUT_VERSION),
            arguments(CheckoutPlatform.ANDROID, null, false, "android/adyen-components", BuildConfig.CHECKOUT_VERSION),
            arguments(CheckoutPlatform.FLUTTER, "1.2.3", true, "flutter/adyen-dropin", "1.2.3"),
            arguments(CheckoutPlatform.FLUTTER, "3.2.1", false, "flutter/adyen-components", "3.2.1"),
            arguments(CheckoutPlatform.REACT_NATIVE, "1.2.3", true, "react-native/adyen-dropin", "1.2.3"),
            arguments(CheckoutPlatform.REACT_NATIVE, "3.2.1", false, "react-native/adyen-components", "3.2.1"),
        )
    }
}
