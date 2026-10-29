/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.AdditionalSessionParams
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.dropin.DropInConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class DropInParamsMapperTest {

    private val mapper = DropInParamsMapper()

    @Test
    fun `when drop-in configuration is null then default values are used`() {
        val params = mapper.mapToParams(createCheckoutParams(dropInConfiguration = null))

        assertEquals(
            DropInParams(
                hideStoredPaymentMethods = false,
                startWithLastStoredPaymentMethod = true,
                isRemovingStoredPaymentMethodsEnabled = false,
            ),
            params,
        )
    }

    @Test
    fun `when hideStoredPaymentMethods is null then it defaults to false`() {
        val params = mapper.mapToParams(
            createCheckoutParams(dropInConfiguration = createDropInConfiguration(hideStoredPaymentMethods = null)),
        )

        assertEquals(false, params.hideStoredPaymentMethods)
    }

    @Test
    fun `when hideStoredPaymentMethods is true then it is passed through`() {
        val params = mapper.mapToParams(
            createCheckoutParams(dropInConfiguration = createDropInConfiguration(hideStoredPaymentMethods = true)),
        )

        assertEquals(true, params.hideStoredPaymentMethods)
    }

    @Test
    fun `when startWithLastStoredPaymentMethod is null then it defaults to true`() {
        val params = mapper.mapToParams(
            createCheckoutParams(
                dropInConfiguration = createDropInConfiguration(startWithLastStoredPaymentMethod = null),
            ),
        )

        assertEquals(true, params.startWithLastStoredPaymentMethod)
    }

    @Test
    fun `when startWithLastStoredPaymentMethod is false then it is passed through`() {
        val params = mapper.mapToParams(
            createCheckoutParams(
                dropInConfiguration = createDropInConfiguration(startWithLastStoredPaymentMethod = false),
            ),
        )

        assertEquals(false, params.startWithLastStoredPaymentMethod)
    }

    @Test
    fun `when the session shows the remove payment method button, then removing is enabled`() {
        val params = mapper.mapToParams(
            createCheckoutParams(additionalSessionParams = createSessionParams(showRemovePaymentMethodButton = true)),
        )

        assertEquals(true, params.isRemovingStoredPaymentMethodsEnabled)
    }

    @Test
    fun `when the session hides the remove payment method button, then removing is disabled`() {
        val params = mapper.mapToParams(
            createCheckoutParams(additionalSessionParams = createSessionParams(showRemovePaymentMethodButton = false)),
        )

        assertEquals(false, params.isRemovingStoredPaymentMethodsEnabled)
    }

    @Test
    fun `when the session does not set the remove payment method button, then removing is disabled`() {
        val params = mapper.mapToParams(
            createCheckoutParams(additionalSessionParams = createSessionParams(showRemovePaymentMethodButton = null)),
        )

        assertEquals(false, params.isRemovingStoredPaymentMethodsEnabled)
    }

    @Test
    fun `when there is no session, then removing is disabled`() {
        val params = mapper.mapToParams(createCheckoutParams(additionalSessionParams = null))

        assertEquals(false, params.isRemovingStoredPaymentMethodsEnabled)
    }

    private fun createDropInConfiguration(
        hideStoredPaymentMethods: Boolean? = null,
        startWithLastStoredPaymentMethod: Boolean? = null,
    ) = DropInConfiguration(
        hideStoredPaymentMethods = hideStoredPaymentMethods,
        startWithLastStoredPaymentMethod = startWithLastStoredPaymentMethod,
    )

    private fun createSessionParams(showRemovePaymentMethodButton: Boolean?) = AdditionalSessionParams(
        enableStoreDetails = null,
        installmentConfiguration = null,
        showRemovePaymentMethodButton = showRemovePaymentMethodButton,
        returnUrl = null,
    )

    private fun createCheckoutParams(
        dropInConfiguration: DropInConfiguration? = createDropInConfiguration(),
        additionalSessionParams: AdditionalSessionParams? = null,
    ) = CheckoutParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = true,
        publicKey = null,
        additionalConfigurations = buildMap {
            dropInConfiguration?.let { this[DropInConfiguration::class.java.name] = it }
        },
        additionalSessionParams = additionalSessionParams,
    )

    private companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
