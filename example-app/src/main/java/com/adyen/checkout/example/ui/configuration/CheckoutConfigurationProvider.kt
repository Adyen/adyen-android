package com.adyen.checkout.example.ui.configuration

import android.content.Context
import com.adyen.checkout.adyen3ds2.adyen3DS2Configuration
import com.adyen.checkout.bacs.BacsDirectDebitConfiguration
import com.adyen.checkout.bacs.bacsDirectDebitConfiguration
import com.adyen.checkout.bacs.getBacsDirectDebitConfiguration
import com.adyen.checkout.bcmc.bcmcConfiguration
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.blik.blikConfiguration
import com.adyen.checkout.blik.getBlikConfiguration
import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.card.cardConfiguration
import com.adyen.checkout.card.getCardConfiguration
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.cashAppPayConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.dropInConfiguration
import com.adyen.checkout.dropin.getDropInConfiguration
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.CardAddressMode
import com.adyen.checkout.example.data.storage.CardInstallmentOptionsMode
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.giftcard.getGiftCardConfiguration
import com.adyen.checkout.giftcard.giftCardConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.getGooglePayConfiguration
import com.adyen.checkout.googlepay.googlePayConfiguration
import com.adyen.checkout.instant.InstantPaymentConfiguration
import com.adyen.checkout.instant.getInstantPaymentConfiguration
import com.adyen.checkout.instant.instantPaymentConfiguration
import com.adyen.checkout.redirect.redirectConfiguration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
internal class CheckoutConfigurationProvider @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
    @ApplicationContext private val context: Context,
) {

    private val shopperLocale: Locale
        get() {
            val shopperLocaleString = keyValueStorage.getShopperLocale()
            return Locale.forLanguageTag(shopperLocaleString)
        }

    private val amount: Amount get() = keyValueStorage.getAmount()

    private val clientKey = BuildConfig.CLIENT_KEY

    private val environment = Environment.TEST

    private val checkoutConfig = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = getAnalyticsConfiguration(),
    ) {
        // Drop-in
        dropInConfiguration {
            setEnableRemovingStoredPaymentMethods(true)
        }

        // Payment methods
        bacsDirectDebitConfiguration()

        bcmcConfiguration {
            setShopperReference(keyValueStorage.getShopperReference())
            setShowStorePaymentField(true)
        }

        blikConfiguration()

        cardConfiguration {
            setShopperReference(keyValueStorage.getShopperReference())
            setAddressConfiguration(getAddressConfiguration())
            setInstallmentConfigurations(getInstallmentConfiguration())
        }

        cashAppPayConfiguration {
            setReturnUrl(CashAppPayComponent.getReturnUrl(context))
        }

        giftCardConfiguration {
            setPinRequired(true)
        }

        googlePayConfiguration {
            setCountryCode(keyValueStorage.getCountry())
        }

        instantPaymentConfiguration()

        // Actions
        adyen3DS2Configuration()

        redirectConfiguration()
    }

    private fun getAnalyticsConfiguration(): AnalyticsConfiguration {
        val analyticsLevel = keyValueStorage.getAnalyticsLevel()
        return AnalyticsConfiguration(level = analyticsLevel)
    }

    fun getDropInConfiguration(): DropInConfiguration = checkoutConfig.getDropInConfiguration()!!

    fun getBacsConfiguration(): BacsDirectDebitConfiguration = checkoutConfig.getBacsDirectDebitConfiguration()!!

    fun getBlikConfiguration(): BlikConfiguration = checkoutConfig.getBlikConfiguration()!!

    fun getCardConfiguration(): CardConfiguration = checkoutConfig.getCardConfiguration()!!

    fun getGiftCardConfiguration(): GiftCardConfiguration = checkoutConfig.getGiftCardConfiguration()!!

    fun getGooglePayConfiguration(): GooglePayConfiguration = checkoutConfig.getGooglePayConfiguration()!!

    fun getInstantConfiguration(): InstantPaymentConfiguration = checkoutConfig.getInstantPaymentConfiguration()!!

    private fun getAddressConfiguration(): AddressConfiguration = when (keyValueStorage.getCardAddressMode()) {
        CardAddressMode.NONE -> AddressConfiguration.None
        CardAddressMode.POSTAL_CODE -> AddressConfiguration.PostalCode()
        CardAddressMode.FULL_ADDRESS -> AddressConfiguration.FullAddress(
            defaultCountryCode = null,
            supportedCountryCodes = listOf("NL", "GB", "US", "CA", "BR"),
            addressFieldPolicy = AddressConfiguration.CardAddressFieldPolicy.OptionalForCardTypes(
                brands = listOf("jcb"),
            ),
        )
    }

    private fun getInstallmentConfiguration(): InstallmentConfiguration =
        when (keyValueStorage.getInstallmentOptionsMode()) {
            CardInstallmentOptionsMode.NONE -> InstallmentConfiguration()
            CardInstallmentOptionsMode.DEFAULT -> getDefaultInstallmentOptions()
            CardInstallmentOptionsMode.DEFAULT_WITH_REVOLVING -> getDefaultInstallmentOptions(includeRevolving = true)
            CardInstallmentOptionsMode.CARD_BASED_VISA -> getCardBasedInstallmentOptions()
        }

    private fun getDefaultInstallmentOptions(
        maxInstallments: Int = 3,
        includeRevolving: Boolean = false
    ) = InstallmentConfiguration(
        defaultOptions = InstallmentOptions.DefaultInstallmentOptions(
            maxInstallments = maxInstallments,
            includeRevolving = includeRevolving,
        ),
        showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
    )

    private fun getCardBasedInstallmentOptions(
        maxInstallments: Int = 3,
        includeRevolving: Boolean = false,
        cardBrand: CardBrand = CardBrand(CardType.VISA)
    ) = InstallmentConfiguration(
        cardBasedOptions = listOf(
            InstallmentOptions.CardBasedInstallmentOptions(
                maxInstallments = maxInstallments,
                includeRevolving = includeRevolving,
                cardBrand = cardBrand,
            ),
        ),
        showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
    )
}
