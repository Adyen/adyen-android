package com.adyen.checkout.action.core

import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.old.AwaitConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.Locale

internal class GenericActionConfigurationTest {

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = GenericActionConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .add3ds2ActionConfiguration(
                Adyen3DS2Configuration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            )
            .addAwaitActionConfiguration(
                AwaitConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            )
            .addQRCodeActionConfiguration(
                QRCodeConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            )
            .addRedirectActionConfiguration(
                RedirectConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            )
            .addVoucherActionConfiguration(
                VoucherConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            )
            .addWeChatPayActionConfiguration(
                WeChatPayActionConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            )
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

        assertNotNull(actual.getActionConfiguration(Adyen3DS2Configuration::class.java))
        assertNotNull(actual.getActionConfiguration(AwaitConfiguration::class.java))
        assertNotNull(actual.getActionConfiguration(QRCodeConfiguration::class.java))
        assertNotNull(actual.getActionConfiguration(RedirectConfiguration::class.java))
        assertNotNull(actual.getActionConfiguration(VoucherConfiguration::class.java))
        assertNotNull(actual.getActionConfiguration(WeChatPayActionConfiguration::class.java))
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
