/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/11/2026.
 */

package com.adyen.checkout.example

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.ext.list.withPackage
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
            .filter {
                // v5 configurations implement a Configuration interface with the same name in another package.
                it.hasParentInterfaceWithName(CONFIGURATION_NAME) && it.containingFile.hasImportWithName(CONFIGURATION)
            }
            .assertTrue(
                strict = true,
                additionalMessage = "Configurations must be data classes so they compare by value. Nested types " +
                    "need value equality as well.",
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
        private const val CONFIGURATION_NAME = "Configuration"
        private const val CONFIGURATION_PACKAGE = "com.adyen.checkout.core.components.internal"
        private const val CONFIGURATION = "$CONFIGURATION_PACKAGE.$CONFIGURATION_NAME"
        private const val CHECKOUT_CONFIGURATION_NAME = "CheckoutConfiguration"
        private const val CHECKOUT_CONFIGURATION_PACKAGE = "com.adyen.checkout.core.components"
    }
}
