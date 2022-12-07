/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/11/2022.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.base.GenericComponentParams
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.core.api.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class CardComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom card configuration fields are null then all fields should match`() {
        val cardConfiguration = getCardConfigurationBuilder().build()

        val params = CardComponentParamsMapper(null).mapToParamsDefault(cardConfiguration, PaymentMethod())

        val expected = getCardComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom card configuration fields are set then all fields should match`() {
        val shopperReference = "SHOPPER_REFERENCE_1"
        val installmentConfiguration = InstallmentConfiguration(
            InstallmentOptions.DefaultInstallmentOptions(
                maxInstallments = 3,
                includeRevolving = true
            )
        )
        val addressConfiguration = AddressConfiguration.FullAddress(supportedCountryCodes = listOf("CA", "GB"))

        val cardConfiguration = CardConfiguration.Builder(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2
        )
            .setHolderNameRequired(true)
            .setSupportedCardTypes(CardType.DINERS, CardType.MAESTRO)
            .setShopperReference(shopperReference)
            .setShowStorePaymentField(false)
            .setHideCvc(true)
            .setHideCvcStoredCard(true)
            .setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
            .setKcpAuthVisibility(KCPAuthVisibility.SHOW)
            .setInstallmentConfigurations(installmentConfiguration)
            .setAddressConfiguration(addressConfiguration)
            .build()

        val params = CardComponentParamsMapper(null).mapToParamsDefault(cardConfiguration, PaymentMethod())

        val expected = getCardComponentParams(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
            isHolderNameRequired = true,
            supportedCardTypes = listOf(CardType.DINERS, CardType.MAESTRO),
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = false,
            isHideCvc = true,
            isHideCvcStoredCard = true,
            socialSecurityNumberVisibility = SocialSecurityNumberVisibility.SHOW,
            kcpAuthVisibility = KCPAuthVisibility.SHOW,
            installmentConfiguration = installmentConfiguration,
            addressConfiguration = addressConfiguration
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override card configuration fields`() {
        val cardConfiguration = getCardConfigurationBuilder().build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = GenericComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
        )

        val params = CardComponentParamsMapper(overrideParams).mapToParamsDefault(cardConfiguration, PaymentMethod())

        val expected = getCardComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when supported card types are set in the card configuration then they should be used in the params`() {
        val cardConfiguration = getCardConfigurationBuilder()
            .setSupportedCardTypes(CardType.MAESTRO, CardType.BCMC)
            .build()

        val paymentMethod = PaymentMethod(brands = listOf(CardType.VISA.txVariant, CardType.MASTERCARD.txVariant))

        val params = CardComponentParamsMapper(null).mapToParamsDefault(cardConfiguration, paymentMethod)

        val expected = getCardComponentParams(
            supportedCardTypes = listOf(CardType.MAESTRO, CardType.BCMC)
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when supported card types are not set in the card configuration and payment method brands exist then brands should be used in the params`() {
        val cardConfiguration = getCardConfigurationBuilder()
            .build()

        val paymentMethod = PaymentMethod(brands = listOf(CardType.VISA.txVariant, CardType.MASTERCARD.txVariant))

        val params = CardComponentParamsMapper(null).mapToParamsDefault(cardConfiguration, paymentMethod)

        val expected = getCardComponentParams(
            supportedCardTypes = listOf(CardType.VISA, CardType.MASTERCARD)
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when supported card types are not set in the card configuration and payment method brands do not exist then the default card types should be used in the params`() {
        val cardConfiguration = getCardConfigurationBuilder()
            .build()

        val params = CardComponentParamsMapper(null).mapToParamsDefault(cardConfiguration, PaymentMethod())

        val expected = getCardComponentParams(
            supportedCardTypes = CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST
        )

        assertEquals(expected, params)
    }

    private fun getCardConfigurationBuilder() = CardConfiguration.Builder(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1
    )

    private fun getCardComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        isAnalyticsEnabled: Boolean = true,
        isCreatedByDropIn: Boolean = false,
        isHolderNameRequired: Boolean = false,
        supportedCardTypes: List<CardType> = CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST,
        shopperReference: String? = null,
        isStorePaymentFieldVisible: Boolean = true,
        isHideCvc: Boolean = false,
        isHideCvcStoredCard: Boolean = false,
        socialSecurityNumberVisibility: SocialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
        kcpAuthVisibility: KCPAuthVisibility = KCPAuthVisibility.HIDE,
        installmentConfiguration: InstallmentConfiguration? = null,
        addressConfiguration: AddressConfiguration = AddressConfiguration.None,
    ) = CardComponentParams(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        isAnalyticsEnabled = isAnalyticsEnabled,
        isCreatedByDropIn = isCreatedByDropIn,
        isHolderNameRequired = isHolderNameRequired,
        supportedCardTypes = supportedCardTypes,
        shopperReference = shopperReference,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible,
        isHideCvc = isHideCvc,
        isHideCvcStoredCard = isHideCvcStoredCard,
        socialSecurityNumberVisibility = socialSecurityNumberVisibility,
        kcpAuthVisibility = kcpAuthVisibility,
        installmentConfiguration = installmentConfiguration,
        addressConfiguration = addressConfiguration
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
    }
}
