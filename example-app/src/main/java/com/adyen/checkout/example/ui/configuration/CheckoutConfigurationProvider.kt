package com.adyen.checkout.example.ui.configuration

import android.content.Context
import com.adyen.checkout.adyen3ds2.adyen3DS2
import com.adyen.checkout.bcmc.bcmc
import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.card.card
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.cashAppPay
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.dropin.dropIn
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.CardAddressMode
import com.adyen.checkout.example.data.storage.CardInstallmentOptionsMode
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.giftcard.giftCard
import com.adyen.checkout.googlepay.googlePay
import com.adyen.checkout.instant.ActionHandlingMethod
import com.adyen.checkout.instant.instantPayment
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

    private val shopperLocale: Locale?
        get() {
            val shopperLocaleString = keyValueStorage.getShopperLocale()
            return shopperLocaleString?.let { Locale.forLanguageTag(it) }
        }

    private val amount: Amount get() = keyValueStorage.getAmount()

    private val clientKey = BuildConfig.CLIENT_KEY

    private val environment = Environment.TEST

    val checkoutConfig: CheckoutConfiguration
        get() = CheckoutConfiguration(
            environment = environment,
            clientKey = clientKey,
            shopperLocale = shopperLocale,
            amount = amount,
            analyticsConfiguration = getAnalyticsConfiguration(),
        ) {
            // Drop-in
            dropIn {
                setEnableRemovingStoredPaymentMethods(keyValueStorage.isRemoveStoredPaymentMethodEnabled())
            }

            // Payment methods
            bcmc {
                setShopperReference(keyValueStorage.getShopperReference())
                setShowStorePaymentField(true)
            }

            card {
                setShopperReference(keyValueStorage.getShopperReference())
                setAddressConfiguration(getAddressConfiguration())
                setInstallmentConfigurations(getInstallmentConfiguration())
            }

            cashAppPay {
                setReturnUrl(CashAppPayComponent.getReturnUrl(context))
            }

            giftCard {
                setPinRequired(true)
            }

            googlePay {
                setCountryCode(keyValueStorage.getCountry())
            }

            instantPayment {
                setActionHandlingMethod(ActionHandlingMethod.PREFER_NATIVE)
            }

            // Actions
            adyen3DS2 {
                setThreeDSRequestorAppURL("https://www.adyen.com")
            }
        }

    private fun getAnalyticsConfiguration(): AnalyticsConfiguration {
        val analyticsLevel = keyValueStorage.getAnalyticsLevel()
        return AnalyticsConfiguration(level = analyticsLevel)
    }

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

        CardAddressMode.LOOKUP -> AddressConfiguration.Lookup()
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
