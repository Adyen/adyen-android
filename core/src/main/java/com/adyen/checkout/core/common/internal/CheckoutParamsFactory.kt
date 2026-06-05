/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/5/2026.
 */

package com.adyen.checkout.core.common.internal

import androidx.appcompat.app.AppCompatDelegate
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.LocaleUtil
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.AnalyticsConfiguration
import com.adyen.checkout.core.components.AnalyticsLevel
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentConfiguration
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentOptionsParams
import java.util.Locale

internal class CheckoutParamsFactory(
    private val deviceLocaleProvider: () -> Locale = {
        AppCompatDelegate.getApplicationLocales().takeUnless { it.isEmpty }?.get(0) ?: Locale.getDefault()
    },
) {

    fun create(
        configuration: CheckoutConfiguration,
        session: CheckoutSession?,
        publicKey: String?,
    ): CheckoutParams {
        return CheckoutParams(
            shopperLocale = configuration.shopperLocale ?: session?.getShopperLocale() ?: deviceLocaleProvider(),
            environment = session?.environment ?: configuration.environment,
            clientKey = session?.clientKey ?: configuration.clientKey,
            analyticsParams = AnalyticsParams(
                level = getAnalyticsLevel(configuration.analyticsConfiguration),
            ),
            amount = session?.sessionSetupResponse?.amount ?: configuration.amount,
            showSubmitButton = configuration.showSubmitButton ?: true,
            publicKey = publicKey,
            additionalConfigurations = configuration.getAvailableConfigurations(),
            additionalSessionParams = session?.createAdditionalSessionParams(),
        )
    }

    private fun CheckoutSession.getShopperLocale(): Locale? {
        val shopperLocaleString = sessionSetupResponse.shopperLocale ?: return null
        return runCatching {
            LocaleUtil.fromLanguageTag(shopperLocaleString)
        }.getOrElse {
            // if we cannot parse the locale coming from the API we should not fail the payment
            adyenLog(AdyenLogLevel.ERROR) { "Failed to parse sessions locale $shopperLocaleString" }
            null
        }
    }

    private fun getAnalyticsLevel(analyticsConfiguration: AnalyticsConfiguration?): AnalyticsParamsLevel {
        return when (analyticsConfiguration?.level) {
            null -> AnalyticsParamsLevel.ALL // default is ALL
            AnalyticsLevel.ALL -> AnalyticsParamsLevel.ALL
            AnalyticsLevel.NONE -> AnalyticsParamsLevel.INITIAL
        }
    }

    private fun CheckoutSession.createAdditionalSessionParams(): AdditionalSessionParams {
        val setupConfig = sessionSetupResponse.configuration
        return AdditionalSessionParams(
            enableStoreDetails = setupConfig?.enableStoreDetails,
            installmentConfiguration = SessionInstallmentConfiguration(
                installmentOptions = setupConfig?.installmentOptions?.mapValues { entry ->
                    entry.value?.let {
                        SessionInstallmentOptionsParams(
                            plans = it.plans,
                            preselectedValue = it.preselectedValue,
                            values = it.values,
                        )
                    }
                },
                showInstallmentAmount = setupConfig?.showInstallmentAmount,
            ),
            showRemovePaymentMethodButton = setupConfig?.showRemovePaymentMethodButton,
            returnUrl = sessionSetupResponse.returnUrl,
        )
    }
}
