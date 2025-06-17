/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/3/2023.
 */

package com.adyen.checkout.sessions.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentOptionsParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.LocaleUtil
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import com.adyen.checkout.sessions.core.internal.data.model.mapToDetails
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
                installmentOptions = sessionSetupConfiguration?.installmentOptions?.map {
                    it.key to SessionInstallmentOptionsParams(
                        plans = it.value?.plans,
                        preselectedValue = it.value?.preselectedValue,
                        values = it.value?.values,
                    )
                }?.toMap(),
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
