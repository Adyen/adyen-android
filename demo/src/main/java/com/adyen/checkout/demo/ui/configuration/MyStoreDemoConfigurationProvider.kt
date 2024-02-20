/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/2/2024.
 */

package com.adyen.checkout.demo.ui.configuration

import android.content.Context
import com.adyen.checkout.adyen3ds2.adyen3DS2
import com.adyen.checkout.bcmc.bcmc
import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.card.card
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.cashAppPay
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.demo.BuildConfig
import com.adyen.checkout.dropin.dropIn
import com.adyen.checkout.giftcard.giftCard
import com.adyen.checkout.googlepay.googlePay
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class MyStoreDemoConfigurationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val clientKey = BuildConfig.CLIENT_KEY

    private val environment = Environment.TEST

    fun getCheckoutConfiguration(amount: Amount) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = getAnalyticsConfiguration(),
    ) {
        // Drop-in
        dropIn {
            setEnableRemovingStoredPaymentMethods(true)
        }

        // Payment methods
        bcmc {
            setShowStorePaymentField(true)
        }

        card {
            setAddressConfiguration(AddressConfiguration.Lookup())
            setInstallmentConfigurations(getInstallmentConfiguration())
        }

        cashAppPay {
            setReturnUrl(CashAppPayComponent.getReturnUrl(context))
        }

        giftCard {
            setPinRequired(true)
        }

        googlePay {
            setCountryCode("nl")
        }

        // Actions
        adyen3DS2 {
            setThreeDSRequestorAppURL("https://www.adyen.com")
        }
    }

    private fun getAnalyticsConfiguration(): AnalyticsConfiguration {
        return AnalyticsConfiguration(level = AnalyticsLevel.ALL)
    }

    private fun getInstallmentConfiguration(): InstallmentConfiguration = getDefaultInstallmentOptions()

    private fun getDefaultInstallmentOptions(
        maxInstallments: Int = 3,
        includeRevolving: Boolean = false
    ) = InstallmentConfiguration(
        defaultOptions = InstallmentOptions.DefaultInstallmentOptions(
            maxInstallments = maxInstallments,
            includeRevolving = includeRevolving,
        ),
        showInstallmentAmount = false,
    )
}
