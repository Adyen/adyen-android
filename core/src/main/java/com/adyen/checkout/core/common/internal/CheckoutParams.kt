/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/5/2026.
 */

package com.adyen.checkout.core.common.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.Configuration
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CheckoutParams(
    val shopperLocale: Locale,
    val environment: Environment,
    val clientKey: String,
    val analyticsParams: AnalyticsParams,
    val amount: Amount?,
    val showSubmitButton: Boolean,
    val publicKey: String?,
    val integrationType: IntegrationType,
    val additionalConfigurations: Map<String, Configuration>,
    val additionalSessionParams: AdditionalSessionParams?,
) {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    inline fun <reified T : Configuration> getPaymentConfiguration(key: String): T? {
        return additionalConfigurations[key] as? T
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    inline fun <reified T : Configuration> getActionConfiguration(configClass: Class<T>): T? {
        return additionalConfigurations[configClass.name] as? T
    }
}
