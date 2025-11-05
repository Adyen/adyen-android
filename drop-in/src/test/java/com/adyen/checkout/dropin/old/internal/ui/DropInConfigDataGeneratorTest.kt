/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.ui

import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.dropin.old.internal.ui.model.DropInParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

internal class DropInConfigDataGeneratorTest {

    private lateinit var dropInConfigDataGenerator: DropInConfigDataGenerator

    @BeforeEach
    fun beforeEach() {
        dropInConfigDataGenerator = DropInConfigDataGenerator()
    }

    @Test
    fun `when generating config data, then fields are correctly mapped`() {
        val result = dropInConfigDataGenerator.generate(createDropInParams())

        val expected = mapOf(
            "skipPaymentMethodList" to "false",
            "openFirstStoredPaymentMethod" to "true",
            "isRemovingStoredPaymentMethodsEnabled" to "true",
        )
        assertEquals(expected, result)
    }

    private fun createDropInParams() = DropInParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "clientKey",
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, "clientKey"),
        amount = null,
        showPreselectedStoredPaymentMethod = true,
        skipListWhenSinglePaymentMethod = false,
        isRemovingStoredPaymentMethodsEnabled = true,
        additionalDataForDropInService = null,
        overriddenPaymentMethodInformation = emptyMap(),
    )
}
