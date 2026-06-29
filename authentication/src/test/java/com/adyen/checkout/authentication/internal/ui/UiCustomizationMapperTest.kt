/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/6/2026.
 */

package com.adyen.checkout.authentication.internal.ui

import androidx.compose.ui.graphics.Color
import com.adyen.checkout.ui.internal.theme.InternalAttributes
import com.adyen.checkout.ui.internal.theme.InternalColors
import com.adyen.threeds2.customization.UiCustomization.ButtonType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UiCustomizationMapperTest {

    @Test
    fun `when mapping colors and attributes then ui customization is created with the correct values`() {
        val uiCustomization = mapToUiCustomization(COLORS, ATTRIBUTES)

        assertEquals(BACKGROUND, uiCustomization.screenCustomization.backgroundColor)

        with(uiCustomization.toolbarCustomization) {
            assertEquals(TEXT, textColor)
            assertEquals(CONTAINER, backgroundColor)
        }

        with(uiCustomization.labelCustomization) {
            assertEquals(TEXT, textColor)
            assertEquals(TEXT, headingTextColor)
            assertEquals(TEXT_SECONDARY, inputLabelTextColor)
        }

        with(uiCustomization.selectionItemCustomization) {
            assertEquals(TEXT, textColor)
            assertEquals(PRIMARY, selectionIndicatorTintColor)
            assertEquals(CONTAINER_OUTLINE, highlightedBackgroundColor)
            assertEquals(OUTLINE, borderColor)
        }

        with(uiCustomization.textBoxCustomization) {
            assertEquals(TEXT, textColor)
            assertEquals(CONTAINER_OUTLINE, borderColor)
            assertEquals(CORNER_RADIUS, cornerRadius)
        }

        with(uiCustomization.expandableInfoCustomization) {
            assertEquals(TEXT, textColor)
            assertEquals(TEXT_SECONDARY, headingTextColor)
            assertEquals(CONTAINER_OUTLINE, highlightedBackgroundColor)
            assertEquals(OUTLINE, borderColor)
        }
    }

    @Test
    fun `when mapping then primary buttons use primary colors and corner radius`() {
        val uiCustomization = mapToUiCustomization(COLORS, ATTRIBUTES)

        listOf(ButtonType.VERIFY, ButtonType.CONTINUE, ButtonType.NEXT).forEach { buttonType ->
            with(uiCustomization.getButtonCustomization(buttonType)) {
                assertEquals(TEXT_ON_PRIMARY, textColor)
                assertEquals(PRIMARY, backgroundColor)
                assertEquals(CORNER_RADIUS, cornerRadius)
            }
        }
    }

    @Test
    fun `when mapping then secondary buttons use primary text color and corner radius`() {
        val uiCustomization = mapToUiCustomization(COLORS, ATTRIBUTES)

        listOf(ButtonType.CANCEL, ButtonType.RESEND, ButtonType.OPEN_OOB_APP).forEach { buttonType ->
            with(uiCustomization.getButtonCustomization(buttonType)) {
                assertEquals(PRIMARY, textColor)
                assertEquals(CORNER_RADIUS, cornerRadius)
            }
        }
    }

    companion object {
        private const val BACKGROUND = "#010203"
        private const val CONTAINER = "#040506"
        private const val CONTAINER_OUTLINE = "#070809"
        private const val PRIMARY = "#0A0B0C"
        private const val TEXT_ON_PRIMARY = "#0D0E0F"
        private const val OUTLINE = "#1F2021"
        private const val TEXT = "#222324"
        private const val TEXT_SECONDARY = "#252627"
        private const val CORNER_RADIUS = 12

        private val COLORS = InternalColors(
            background = Color(0xFF010203),
            container = Color(0xFF040506),
            containerOutline = Color(0xFF070809),
            primary = Color(0xFF0A0B0C),
            textOnPrimary = Color(0xFF0D0E0F),
            highlight = Color(0xFF101112),
            destructive = Color(0xFF131415),
            textOnDestructive = Color(0xFF161718),
            disabled = Color(0xFF191A1B),
            textOnDisabled = Color(0xFF1C1D1E),
            outline = Color(0xFF1F2021),
            text = Color(0xFF222324),
            textSecondary = Color(0xFF252627),
        )

        private val ATTRIBUTES = InternalAttributes(cornerRadius = CORNER_RADIUS)
    }
}
