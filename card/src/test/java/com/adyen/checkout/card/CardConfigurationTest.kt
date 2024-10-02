package com.adyen.checkout.card

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.CardType
import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class CardConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            card {
                setSupportedCardTypes(CardType.MASTERCARD)
                setHolderNameRequired(true)
                setShowStorePaymentField(true)
                setShopperReference("shopperReference")
                setHideCvc(true)
                setHideCvcStoredCard(true)
                setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
                setKcpAuthVisibility(KCPAuthVisibility.SHOW)
                setInstallmentConfigurations(InstallmentConfiguration(showInstallmentAmount = true))
                setAddressConfiguration(AddressConfiguration.None)
                setSubmitButtonVisible(false)
            }
        }

        val actual = checkoutConfiguration.getCardConfiguration()

        val expected = CardConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setSupportedCardTypes(CardType.MASTERCARD)
            .setHolderNameRequired(true)
            .setShowStorePaymentField(true)
            .setShopperReference("shopperReference")
            .setHideCvc(true)
            .setHideCvcStoredCard(true)
            .setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
            .setKcpAuthVisibility(KCPAuthVisibility.SHOW)
            .setInstallmentConfigurations(InstallmentConfiguration(showInstallmentAmount = true))
            .setAddressConfiguration(AddressConfiguration.None)
            .setSubmitButtonVisible(false)
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(expected.supportedCardBrands, actual?.supportedCardBrands)
        assertEquals(expected.isHolderNameRequired, actual?.isHolderNameRequired)
        assertEquals(expected.isStorePaymentFieldVisible, actual?.isStorePaymentFieldVisible)
        assertEquals(expected.shopperReference, actual?.shopperReference)
        assertEquals(expected.isHideCvc, actual?.isHideCvc)
        assertEquals(expected.isHideCvcStoredCard, actual?.isHideCvcStoredCard)
        assertEquals(expected.socialSecurityNumberVisibility, actual?.socialSecurityNumberVisibility)
        assertEquals(expected.kcpAuthVisibility, actual?.kcpAuthVisibility)
        assertEquals(expected.installmentConfiguration, actual?.installmentConfiguration)
        assertEquals(expected.addressConfiguration, actual?.addressConfiguration)
        assertEquals(expected.isSubmitButtonVisible, actual?.isSubmitButtonVisible)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = CardConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setSupportedCardTypes(CardType.MASTERCARD)
            .setHolderNameRequired(true)
            .setShowStorePaymentField(true)
            .setShopperReference("shopperReference")
            .setHideCvc(true)
            .setHideCvcStoredCard(true)
            .setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
            .setKcpAuthVisibility(KCPAuthVisibility.SHOW)
            .setInstallmentConfigurations(InstallmentConfiguration(showInstallmentAmount = true))
            .setAddressConfiguration(AddressConfiguration.None)
            .setSubmitButtonVisible(false)
            .build()

        val actual = config.toCheckoutConfiguration()

        val expected = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        )

        assertEquals(expected.shopperLocale, actual.shopperLocale)
        assertEquals(expected.environment, actual.environment)
        assertEquals(expected.clientKey, actual.clientKey)
        assertEquals(expected.amount, actual.amount)
        assertEquals(expected.analyticsConfiguration, actual.analyticsConfiguration)

        val actualCardConfig = actual.getCardConfiguration()
        assertEquals(config.shopperLocale, actualCardConfig?.shopperLocale)
        assertEquals(config.environment, actualCardConfig?.environment)
        assertEquals(config.clientKey, actualCardConfig?.clientKey)
        assertEquals(config.amount, actualCardConfig?.amount)
        assertEquals(config.analyticsConfiguration, actualCardConfig?.analyticsConfiguration)
        assertEquals(config.supportedCardBrands, actualCardConfig?.supportedCardBrands)
        assertEquals(config.isHolderNameRequired, actualCardConfig?.isHolderNameRequired)
        assertEquals(config.isStorePaymentFieldVisible, actualCardConfig?.isStorePaymentFieldVisible)
        assertEquals(config.shopperReference, actualCardConfig?.shopperReference)
        assertEquals(config.isHideCvc, actualCardConfig?.isHideCvc)
        assertEquals(config.isHideCvcStoredCard, actualCardConfig?.isHideCvcStoredCard)
        assertEquals(config.socialSecurityNumberVisibility, actualCardConfig?.socialSecurityNumberVisibility)
        assertEquals(config.kcpAuthVisibility, actualCardConfig?.kcpAuthVisibility)
        assertEquals(config.installmentConfiguration, actualCardConfig?.installmentConfiguration)
        assertEquals(config.addressConfiguration, actualCardConfig?.addressConfiguration)
        assertEquals(config.isSubmitButtonVisible, actualCardConfig?.isSubmitButtonVisible)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
