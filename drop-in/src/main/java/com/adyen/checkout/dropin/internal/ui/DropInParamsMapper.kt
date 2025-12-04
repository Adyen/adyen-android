/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.appcompat.app.AppCompatDelegate
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import java.util.Locale

internal class DropInParamsMapper {

    fun map(
        checkoutConfiguration: CheckoutConfiguration,
        sessionParams: SessionParams?,
    ): DropInParams {
        return DropInParams(
            shopperLocale = checkoutConfiguration.shopperLocale
                ?: sessionParams?.shopperLocale
                ?: AppCompatDelegate.getApplicationLocales()[0]
                ?: Locale.getDefault(),
            environment = sessionParams?.environment ?: checkoutConfiguration.environment,
            amount = sessionParams?.amount
                ?: checkoutConfiguration.amount
                ?: error("Amount cannot not be null"),
        )
    }
}
