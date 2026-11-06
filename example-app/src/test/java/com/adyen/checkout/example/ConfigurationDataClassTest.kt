/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/11/2026.
 */

package com.adyen.checkout.example

import com.adyen.checkout.core.components.internal.Configuration
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutAbstractModifier
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withParentInterfaceOf
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/**
 * Configurations compare by value, so tests and merchants can compare them with `==`. A configuration that is a regular
 * class silently falls back to identity equality, for itself and for the `CheckoutConfiguration` that holds it.
 */
internal class ConfigurationDataClassTest {

    @Test
    fun `when class is a configuration, then it is a data class`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withParentInterfaceOf(Configuration::class, indirectParents = true)
            // A shared abstract base can't be a data class, so only the configurations extending it are checked.
            .withoutAbstractModifier()
            .assertTrue(
                strict = true,
                additionalMessage = "Configurations must be data classes so they compare by value. A shared abstract " +
                    "base is allowed, but the configurations extending it must be data classes. Nested types need " +
                    "value equality as well.",
            ) {
                it.hasDataModifier
            }
    }

    @Test
    fun `when class is the checkout configuration, then it is a data class`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withName(CHECKOUT_CONFIGURATION_NAME)
            .withPackage(CHECKOUT_CONFIGURATION_PACKAGE)
            .assertTrue(
                strict = true,
                additionalMessage = "CheckoutConfiguration must be a data class so it compares by value.",
            ) {
                it.hasDataModifier
            }
    }

    companion object {
        private const val CHECKOUT_CONFIGURATION_NAME = "CheckoutConfiguration"
        private const val CHECKOUT_CONFIGURATION_PACKAGE = "com.adyen.checkout.core.components"
    }
}
