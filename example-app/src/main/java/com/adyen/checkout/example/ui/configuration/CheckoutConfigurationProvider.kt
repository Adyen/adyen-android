package com.adyen.checkout.example.ui.configuration

import android.content.Context
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CheckoutConfigurationProvider @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
) {

    private val shopperLocale: Locale
        get() {
            val shopperLocaleString = keyValueStorage.getShopperLocale()
            return Locale.forLanguageTag(shopperLocaleString)
        }

    private val amount: Amount get() = keyValueStorage.getAmount()

    private val clientKey = BuildConfig.CLIENT_KEY

    private val environment = Environment.TEST

    fun getDropInConfiguration(context: Context): DropInConfiguration {
        val dropInConfigurationBuilder = DropInConfiguration.Builder(
            context,
            clientKey
        )
            .setEnvironment(environment)
            .setShopperLocale(shopperLocale)
            .addCardConfiguration(getCardConfiguration())
            .addBcmcConfiguration(getBcmcConfiguration())
            .addGooglePayConfiguration(getGooglePayConfiguration())
            .add3ds2ActionConfiguration(get3DS2Configuration())
            .setEnableRemovingStoredPaymentMethods(true)

        try {
            dropInConfigurationBuilder.setAmount(amount)
        } catch (e: CheckoutException) {
            Logger.e(TAG, "Amount $amount not valid", e)
        }

        return dropInConfigurationBuilder.build()
    }

    fun getCardConfiguration(): CardConfiguration =
        CardConfiguration.Builder(shopperLocale, environment, clientKey)
            .setShopperReference(keyValueStorage.getShopperReference())
            .build()

    private fun getBcmcConfiguration(): BcmcConfiguration =
        BcmcConfiguration.Builder(shopperLocale, environment, clientKey)
            .setShopperReference(keyValueStorage.getShopperReference())
            .setShowStorePaymentField(true)
            .build()

    private fun getGooglePayConfiguration(): GooglePayConfiguration =
        GooglePayConfiguration.Builder(shopperLocale, environment, clientKey)
            .setCountryCode(keyValueStorage.getCountry())
            .setAmount(amount)
            .build()

    fun get3DS2Configuration(): Adyen3DS2Configuration =
        Adyen3DS2Configuration.Builder(shopperLocale, environment, clientKey)
            .build()

    fun getRedirectConfiguration(): RedirectConfiguration =
        RedirectConfiguration.Builder(shopperLocale, environment, clientKey)
            .build()

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
