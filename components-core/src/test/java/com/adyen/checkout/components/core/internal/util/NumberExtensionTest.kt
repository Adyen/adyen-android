/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/11/2023.
 */

package com.adyen.checkout.components.core.internal.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

internal class NumberExtensionTest {

    @Test
    fun `integer format to localized string formats with en-US locale`() {
        val locale = Locale.forLanguageTag("en-US")

        assertEquals("1", 1.formatToLocalizedString(locale))
        assertEquals("5", 5.formatToLocalizedString(locale))
        assertEquals("10", 10.formatToLocalizedString(locale))
        assertEquals("15", 15.formatToLocalizedString(locale))
    }

    @Test
    fun `integer format to localized string formats with ar-LB locale`() {
        val locale = Locale.forLanguageTag("ar-LB")

        assertEquals("١", 1.formatToLocalizedString(locale))
        assertEquals("٥", 5.formatToLocalizedString(locale))
        assertEquals("١٠", 10.formatToLocalizedString(locale))
        assertEquals("١٥", 15.formatToLocalizedString(locale))
    }
}
