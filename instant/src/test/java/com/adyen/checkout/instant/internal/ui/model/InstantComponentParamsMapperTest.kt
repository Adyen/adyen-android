/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/11/2023.
 */

package com.adyen.checkout.instant.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.instant.ActionHandlingMethod
import com.adyen.checkout.instant.instantPayment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class InstantComponentParamsMapperTest {

    private val instantComponentParamsMapper = InstantComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null then params should match the component configuration`() {
        val configuration = createCheckoutConfiguration()

        val params = instantComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null, PAYMENT_METHOD)

        val expected = getInstantComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set, then parent configuration fields should override component configuration fields`() {
        val configuration = createCheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "EUR",
                value = 49_00L,
            ),
        )

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L), null)

        val params = instantComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
            paymentMethod = PAYMENT_METHOD,
        )

        val expected = getInstantComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_2),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 123L,
            ),
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions if it exists, then should match drop in value, then configuration`(
        configurationValue: Amount,
        dropInValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val configuration = createCheckoutConfiguration(amount = configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }

        val sessionParams = createSessionParams(
            amount = sessionsValue,
        )

        val params = instantComponentParamsMapper.mapToParams(
            configuration,
            DEVICE_LOCALE,
            dropInOverrideParams,
            sessionParams,
            PAYMENT_METHOD,
        )

        val expected = getInstantComponentParams(
            amount = expectedValue,
            isCreatedByDropIn = dropInOverrideParams != null,
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("shopperLocaleSource")
    fun `shopper locale should match value set in configuration then sessions then device locale`(
        configurationValue: Locale?,
        sessionsValue: Locale?,
        deviceLocaleValue: Locale,
        expectedValue: Locale,
    ) {
        val configuration = createCheckoutConfiguration(shopperLocale = configurationValue)

        val sessionParams = createSessionParams(
            shopperLocale = sessionsValue,
        )

        val params = instantComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = deviceLocaleValue,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = PAYMENT_METHOD,
        )

        val expected = getInstantComponentParams(
            shopperLocale = expectedValue,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `environment and client key should match value set in sessions`() {
        val configuration = createCheckoutConfiguration()

        val sessionParams = createSessionParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        val params = instantComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = PAYMENT_METHOD,
        )

        val expected = getInstantComponentParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        assertEquals(expected, params)
    }

    @Suppress("LongParameterList")
    private fun getInstantComponentParams(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_1),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
    ): InstantComponentParams {
        return InstantComponentParams(
            commonComponentParams = CommonComponentParams(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsParams = analyticsParams,
                isCreatedByDropIn = isCreatedByDropIn,
                amount = amount,
            ),
            actionHandlingMethod = ActionHandlingMethod.PREFER_NATIVE,
        )
    }

    private fun createCheckoutConfiguration(
        shopperLocale: Locale? = null,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        amount: Amount? = null,
        actionHandlingMethod: ActionHandlingMethod? = null,
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
    ) {
        instantPayment {
            actionHandlingMethod?.let { setActionHandlingMethod(it) }
        }
    }

    @Suppress("LongParameterList")
    private fun createSessionParams(
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        enableStoreDetails: Boolean? = null,
        installmentConfiguration: SessionInstallmentConfiguration? = null,
        showRemovePaymentMethodButton: Boolean? = null,
        amount: Amount? = null,
        returnUrl: String? = "",
        shopperLocale: Locale? = null,
    ) = SessionParams(
        environment = environment,
        clientKey = clientKey,
        enableStoreDetails = enableStoreDetails,
        installmentConfiguration = installmentConfiguration,
        showRemovePaymentMethodButton = showRemovePaymentMethodButton,
        amount = amount,
        returnUrl = returnUrl,
        shopperLocale = shopperLocale,
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private val DEVICE_LOCALE = Locale("nl", "NL")
        private val PAYMENT_METHOD = PaymentMethod(type = "paypal")

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, dropInValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("USD", 200), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), Amount("USD", 200), null, Amount("USD", 200)),
            arguments(Amount("EUR", 100), null, null, Amount("EUR", 100)),
        )

        @JvmStatic
        fun shopperLocaleSource() = listOf(
            // configurationValue, sessionsValue, deviceLocaleValue, expectedValue
            arguments(null, null, Locale.US, Locale.US),
            arguments(Locale.GERMAN, null, Locale.US, Locale.GERMAN),
            arguments(null, Locale.CHINESE, Locale.US, Locale.CHINESE),
            arguments(Locale.GERMAN, Locale.CHINESE, Locale.US, Locale.GERMAN),
        )
    }
}
