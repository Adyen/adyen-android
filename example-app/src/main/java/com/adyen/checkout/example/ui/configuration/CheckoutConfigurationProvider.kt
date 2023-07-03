package com.adyen.checkout.example.ui.configuration

import android.content.Context
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.bacs.BacsDirectDebitConfiguration
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.Environment
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.instant.InstantPaymentConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
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

    fun getDropInConfiguration(): DropInConfiguration = DropInConfiguration.Builder(
        shopperLocale,
        environment,
        clientKey,
    )
        .addCardConfiguration(getCardConfiguration())
        .addCashAppPayConfiguration(getCashAppPayConfiguration())
        .addBcmcConfiguration(getBcmcConfiguration())
        .addGooglePayConfiguration(getGooglePayConfiguration())
        .add3ds2ActionConfiguration(get3DS2Configuration())
        .addRedirectActionConfiguration(getRedirectConfiguration())
        .setEnableRemovingStoredPaymentMethods(true)
        .setAmount(amount)
        .build()

    fun getCardConfiguration(): CardConfiguration =
        CardConfiguration.Builder(shopperLocale, environment, clientKey)
            .setShopperReference(keyValueStorage.getShopperReference())
            .setAddressConfiguration(getAddressConfiguration())
            .setInstallmentConfigurations(getInstallmentConfiguration())
            .setAmount(amount)
            .build()

    private fun getCashAppPayConfiguration(): CashAppPayConfiguration =
        CashAppPayConfiguration.Builder(shopperLocale, environment, clientKey)
            .setReturnUrl(CashAppPayComponent.getReturnUrl(context))
            .build()

    fun getBlikConfiguration(): BlikConfiguration =
        BlikConfiguration.Builder(shopperLocale, environment, clientKey)
            .setAmount(amount)
            .build()

    fun getBacsConfiguration(): BacsDirectDebitConfiguration =
        BacsDirectDebitConfiguration.Builder(shopperLocale, environment, clientKey)
            .setAmount(amount)
            .build()

    fun getInstantConfiguration(): InstantPaymentConfiguration =
        InstantPaymentConfiguration.Builder(shopperLocale, environment, clientKey)
            .setAmount(amount)
            .build()

    fun getGiftCardConfiguration(): GiftCardConfiguration =
        GiftCardConfiguration.Builder(shopperLocale, environment, clientKey)
            .setAmount(amount)
            .build()

    private fun getAddressConfiguration(): AddressConfiguration = when (keyValueStorage.isAddressFormEnabled()) {
        0 -> AddressConfiguration.None
        1 -> AddressConfiguration.PostalCode()
        else -> AddressConfiguration.FullAddress(
            defaultCountryCode = null,
            supportedCountryCodes = listOf("NL", "GB", "US", "CA", "BR"),
            addressFieldPolicy = AddressConfiguration.CardAddressFieldPolicy.OptionalForCardTypes(
                brands = listOf("jcb")
            )
        )
    }

    private fun getBcmcConfiguration(): BcmcConfiguration =
        BcmcConfiguration.Builder(shopperLocale, environment, clientKey)
            .setShopperReference(keyValueStorage.getShopperReference())
            .setShowStorePaymentField(true)
            .setAmount(amount)
            .build()

    private fun getGooglePayConfiguration(): GooglePayConfiguration =
        GooglePayConfiguration.Builder(shopperLocale, environment, clientKey)
            .setCountryCode(keyValueStorage.getCountry())
            .setAmount(amount)
            .build()

    private fun get3DS2Configuration(): Adyen3DS2Configuration =
        Adyen3DS2Configuration.Builder(shopperLocale, environment, clientKey)
            .setAmount(amount)
            .build()

    private fun getRedirectConfiguration(): RedirectConfiguration =
        RedirectConfiguration.Builder(shopperLocale, environment, clientKey)
            .setAmount(amount)
            .build()

    private fun getInstallmentConfiguration(): InstallmentConfiguration =
        when (keyValueStorage.getInstallmentOptionsMode()) {
            0 -> InstallmentConfiguration()
            1 -> getDefaultInstallmentOptions()
            2 -> getDefaultInstallmentOptions(includeRevolving = true)
            else -> getCardBasedInstallmentOptions()
        }

    private fun getDefaultInstallmentOptions(
        maxInstallments: Int = 3,
        includeRevolving: Boolean = false
    ) = InstallmentConfiguration(
        InstallmentOptions.DefaultInstallmentOptions(
            maxInstallments = maxInstallments,
            includeRevolving = includeRevolving
        )
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
                cardBrand = cardBrand
            )
        )
    )
}
