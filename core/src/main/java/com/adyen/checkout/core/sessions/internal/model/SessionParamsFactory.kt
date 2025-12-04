/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/5/2025.
 */

package com.adyen.checkout.core.sessions.internal.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.LocaleUtil
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetails
import com.adyen.checkout.core.sessions.internal.data.model.mapToDetails
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object SessionParamsFactory {
    // Used for components
    fun create(checkoutSession: CheckoutSession): SessionParams {
        return checkoutSession.mapToDetails().mapToParams()
    }

    // Used for Drop-in
    fun create(sessionDetails: SessionDetails): SessionParams {
        return sessionDetails.mapToParams()
    }

    private fun SessionDetails.mapToParams(): SessionParams {
        return SessionParams(
            environment = environment,
            clientKey = clientKey,
            enableStoreDetails = sessionSetupConfiguration?.enableStoreDetails,
            installmentConfiguration = SessionInstallmentConfiguration(
                installmentOptions = sessionSetupConfiguration?.installmentOptions?.mapValues { entry ->
                    entry.value?.let {
                        SessionInstallmentOptionsParams(
                            plans = it.plans,
                            preselectedValue = it.preselectedValue,
                            values = it.values,
                        )
                    }
                },
                showInstallmentAmount = sessionSetupConfiguration?.showInstallmentAmount,
            ),
            showRemovePaymentMethodButton = sessionSetupConfiguration?.showRemovePaymentMethodButton,
            amount = amount,
            returnUrl = returnUrl,
            shopperLocale = getShopperLocale(shopperLocale),
        )
    }

    private fun getShopperLocale(shopperLocaleString: String?): Locale? {
        if (shopperLocaleString == null) return null
        return runCatching {
            LocaleUtil.fromLanguageTag(shopperLocaleString)
        }.getOrElse {
            // if we cannot parse the locale coming from the API we should not fail the payment
            adyenLog(AdyenLogLevel.ERROR) { "Failed to parse sessions locale $shopperLocaleString" }
            null
        }
    }
}
