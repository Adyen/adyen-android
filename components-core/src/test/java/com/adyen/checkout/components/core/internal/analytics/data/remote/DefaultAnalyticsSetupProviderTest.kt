package com.adyen.checkout.components.core.internal.analytics.data.remote

import android.app.Application
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.analytics.AnalyticsPlatformParams
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Locale

internal class DefaultAnalyticsSetupProviderTest {

    private lateinit var analyticsSetupProvider: DefaultAnalyticsSetupProvider

    @Test
    fun `when providing AnalyticsSetupRequest, then it should be mapped correctly`() {
        analyticsSetupProvider = DefaultAnalyticsSetupProvider(
            application = createMockApplication(),
            shopperLocale = Locale.US,
            isCreatedByDropIn = false,
            amount = Amount("USD", 123),
            source = AnalyticsSource.PaymentComponent("scheme"),
            sessionId = "sessionId",
        )

        val result = analyticsSetupProvider.provide()

        val expected = AnalyticsSetupRequest(
            version = AnalyticsPlatformParams.version,
            channel = AnalyticsPlatformParams.channel,
            platform = AnalyticsPlatformParams.platform,
            locale = Locale.US.toLanguageTag(),
            component = "scheme",
            flavor = "components",
            deviceBrand = Build.BRAND,
            deviceModel = Build.MODEL,
            referrer = "com.adyen.checkout",
            systemVersion = Build.VERSION.SDK_INT.toString(),
            containerWidth = null,
            screenWidth = 420,
            paymentMethods = listOf("scheme"),
            amount = Amount("USD", 123),
            sessionId = "sessionId",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `when created by drop in, then flavor should be dropin`() {
        analyticsSetupProvider = DefaultAnalyticsSetupProvider(
            application = createMockApplication(),
            shopperLocale = Locale.US,
            isCreatedByDropIn = true,
            amount = Amount("USD", 123),
            source = AnalyticsSource.PaymentComponent("scheme"),
            sessionId = "sessionId",
        )

        val result = analyticsSetupProvider.provide()

        assertEquals("dropin", result.flavor)
    }

    @Test
    fun `when not created by drop in, then flavor should be components`() {
        analyticsSetupProvider = DefaultAnalyticsSetupProvider(
            application = createMockApplication(),
            shopperLocale = Locale.US,
            isCreatedByDropIn = false,
            amount = Amount("USD", 123),
            source = AnalyticsSource.PaymentComponent("scheme"),
            sessionId = "sessionId",
        )

        val result = analyticsSetupProvider.provide()

        assertEquals("components", result.flavor)
    }

    @Test
    fun `when source is drop in, then component should be dropin`() {
        analyticsSetupProvider = DefaultAnalyticsSetupProvider(
            application = createMockApplication(),
            shopperLocale = Locale.US,
            isCreatedByDropIn = true,
            amount = Amount("USD", 123),
            source = AnalyticsSource.DropIn(listOf()),
            sessionId = "sessionId",
        )

        val result = analyticsSetupProvider.provide()

        assertEquals("dropin", result.component)
    }

    @Test
    fun `when source is a component, then component should be equal to it's payment method`() {
        analyticsSetupProvider = DefaultAnalyticsSetupProvider(
            application = createMockApplication(),
            shopperLocale = Locale.US,
            isCreatedByDropIn = true,
            amount = Amount("USD", 123),
            source = AnalyticsSource.PaymentComponent("scheme"),
            sessionId = "sessionId",
        )

        val result = analyticsSetupProvider.provide()

        assertEquals("scheme", result.component)
    }

    private fun createMockApplication(): Application {
        val application = mock<Application>()
        val resources = mock<Resources>()
        val displayMetrics = DisplayMetrics().apply {
            widthPixels = 420
        }

        whenever(application.packageName) doReturn "com.adyen.checkout"
        whenever(application.resources) doReturn resources
        whenever(resources.displayMetrics) doReturn displayMetrics

        return application
    }
}
