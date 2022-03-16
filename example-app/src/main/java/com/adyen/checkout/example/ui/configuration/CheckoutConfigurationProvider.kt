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
import com.adyen.checkout.example.service.ExampleFullAsyncDropInService
import com.adyen.checkout.googlepay.GooglePayConfiguration
import java.util.*
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

    fun getDropInConfiguration(context: Context): DropInConfiguration {
        val dropInConfigurationBuilder = DropInConfiguration.Builder(
            context,
            ExampleFullAsyncDropInService::class.java,
            BuildConfig.CLIENT_KEY
        )
            .setEnvironment(Environment.TEST)
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

    fun getCardConfiguration() = CardConfiguration.Builder(shopperLocale, Environment.TEST, BuildConfig.CLIENT_KEY)
        .setShopperReference(keyValueStorage.getShopperReference())
        .build()

    private fun getBcmcConfiguration() = BcmcConfiguration.Builder(shopperLocale, Environment.TEST, BuildConfig.CLIENT_KEY)
        .setShopperReference(keyValueStorage.getShopperReference())
        .setShowStorePaymentField(true)
        .build()

    private fun getGooglePayConfiguration()= GooglePayConfiguration.Builder(shopperLocale, Environment.TEST, BuildConfig.CLIENT_KEY)
        .setCountryCode(keyValueStorage.getCountry())
        .setAmount(amount)
        .build()

    private fun get3DS2Configuration() = Adyen3DS2Configuration.Builder(shopperLocale, Environment.TEST, BuildConfig.CLIENT_KEY)
        .build()

    companion object {
        private val TAG: String = LogUtil.getTag()
    }
}
