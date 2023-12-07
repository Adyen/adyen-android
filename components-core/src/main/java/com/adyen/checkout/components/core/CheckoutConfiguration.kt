/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2023.
 */

package com.adyen.checkout.components.core

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import java.util.Locale
import kotlin.reflect.KClass

class CheckoutConfiguration(
    val shopperLocale: Locale,
    val environment: Environment,
    val clientKey: String,
    val amount: Amount? = null,
    val analyticsConfiguration: AnalyticsConfiguration? = null,
    config: CheckoutConfiguration.() -> Unit = {},
) {

    init {
        this.apply(config)
    }

    private val availablePaymentConfigs = mutableMapOf<KClass<out Configuration>, Configuration>()

    fun addConfiguration(configuration: Configuration) {
        availablePaymentConfigs[configuration::class] = configuration
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun <T : Configuration> getConfiguration(configKlass: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return availablePaymentConfigs[configKlass] as? T
    }
}
