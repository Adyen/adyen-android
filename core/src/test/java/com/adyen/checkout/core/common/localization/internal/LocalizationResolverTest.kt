/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/8/2026.
 */

package com.adyen.checkout.core.common.localization.internal

import android.content.Context
import com.adyen.checkout.core.R
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

// The Context is mocked instead of using Robolectric because string resources are not packaged into unit
// tests in this project, so the default localizations cannot be resolved for real here.
@ExtendWith(MockitoExtension::class)
internal class LocalizationResolverTest(
    @Mock private val context: Context,
) {

    @Test
    fun `when the provider returns a string, then that string is used`() {
        val result = getLocalizedString(FakeLocalizationProvider("Custom close"))

        assertEquals("Custom close", result)
    }

    @Test
    fun `when the provider returns null, then the default localization is used`() {
        whenever(context.getString(R.string.checkout_general_close)) doReturn DEFAULT_CLOSE

        val result = getLocalizedString(FakeLocalizationProvider(null))

        assertEquals(DEFAULT_CLOSE, result)
    }

    @Test
    fun `when there is no provider, then the default localization is used`() {
        whenever(context.getString(R.string.checkout_general_close)) doReturn DEFAULT_CLOSE

        val result = getLocalizedString(localizationProvider = null)

        assertEquals(DEFAULT_CLOSE, result)
    }

    @Test
    fun `when the provider is called, then the context locale and key are forwarded`() {
        val provider = FakeLocalizationProvider("Custom cancel")

        getLocalizedString(provider, key = CheckoutLocalizationKey.GENERAL_CANCEL, locale = Locale.FRANCE)

        assertSame(context, provider.lastContext)
        assertEquals(Locale.FRANCE, provider.lastLocale)
        assertEquals(CheckoutLocalizationKey.GENERAL_CANCEL, provider.lastKey)
    }

    // Asserting the same format string across two locales keeps this hermetic: formatting with the device
    // locale returns the same output for both, so one of the cases fails whatever the default locale is.
    @ParameterizedTest
    @MethodSource("shopperLocaleFormattingSource")
    fun `when format args are passed, then the string is formatted using the shopper locale`(
        locale: Locale,
        expected: String,
    ) {
        val result = getLocalizedString(
            localizationProvider = FakeLocalizationProvider("Pay %.2f"),
            key = CheckoutLocalizationKey.PAY_BUTTON_WITH_AMOUNT,
            locale = locale,
            formatArgs = arrayOf(10.0),
        )

        assertEquals(expected, result)
    }

    @Test
    fun `when no format args are passed, then the string is returned unformatted`() {
        // "100%" is not a valid format string, so this would fail if formatting were applied unconditionally.
        val result = getLocalizedString(FakeLocalizationProvider("100%"))

        assertEquals("100%", result)
    }

    private fun getLocalizedString(
        localizationProvider: CheckoutLocalizationProvider?,
        key: CheckoutLocalizationKey = CheckoutLocalizationKey.GENERAL_CLOSE,
        locale: Locale = Locale.US,
        formatArgs: Array<out Any> = emptyArray(),
    ) = LocalizationResolver(localizationProvider).getLocalizedStringFor(context, locale, key, formatArgs)

    private class FakeLocalizationProvider(
        private val localizedString: String?,
    ) : CheckoutLocalizationProvider {

        var lastContext: Context? = null
            private set
        var lastLocale: Locale? = null
            private set
        var lastKey: CheckoutLocalizationKey? = null
            private set

        override fun getLocalizedString(
            context: Context,
            locale: Locale,
            key: CheckoutLocalizationKey,
        ): String? {
            lastContext = context
            lastLocale = locale
            lastKey = key
            return localizedString
        }
    }

    companion object {
        private const val DEFAULT_CLOSE = "Close"

        @JvmStatic
        fun shopperLocaleFormattingSource() = listOf(
            arguments(Locale.US, "Pay 10.00"),
            arguments(Locale.FRANCE, "Pay 10,00"),
        )
    }
}
