/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/6/2026.
 */

package com.adyen.checkout.threeds2.internal.ui

import com.adyen.checkout.ui.internal.helper.toHex
import com.adyen.checkout.ui.internal.theme.InternalAttributes
import com.adyen.checkout.ui.internal.theme.InternalColors
import com.adyen.threeds2.customization.ButtonCustomization
import com.adyen.threeds2.customization.ExpandableInfoCustomization
import com.adyen.threeds2.customization.LabelCustomization
import com.adyen.threeds2.customization.ScreenCustomization
import com.adyen.threeds2.customization.SelectionItemCustomization
import com.adyen.threeds2.customization.TextBoxCustomization
import com.adyen.threeds2.customization.ToolbarCustomization
import com.adyen.threeds2.customization.UiCustomization
import com.adyen.threeds2.customization.UiCustomization.ButtonType

internal fun mapToUiCustomization(
    colors: InternalColors,
    attributes: InternalAttributes,
): UiCustomization = UiCustomization().apply {
    screenCustomization = ScreenCustomization().apply {
        backgroundColor = colors.background.toHex()
    }

    toolbarCustomization = ToolbarCustomization().apply {
        textColor = colors.text.toHex()
        backgroundColor = colors.container.toHex()
    }

    labelCustomization = LabelCustomization().apply {
        textColor = colors.text.toHex()
        headingTextColor = colors.text.toHex()
        inputLabelTextColor = colors.textSecondary.toHex()
    }

    selectionItemCustomization = SelectionItemCustomization().apply {
        textColor = colors.text.toHex()
        selectionIndicatorTintColor = colors.primary.toHex()
        highlightedBackgroundColor = colors.containerOutline.toHex()
        borderColor = colors.outline.toHex()
    }

    textBoxCustomization = TextBoxCustomization().apply {
        textColor = colors.text.toHex()
        borderColor = colors.containerOutline.toHex()
        cornerRadius = attributes.cornerRadius
    }

    val primaryButtonCustomization = ButtonCustomization().apply {
        textColor = colors.textOnPrimary.toHex()
        backgroundColor = colors.primary.toHex()
        cornerRadius = attributes.cornerRadius
    }
    setButtonCustomization(primaryButtonCustomization, ButtonType.VERIFY)
    setButtonCustomization(primaryButtonCustomization, ButtonType.CONTINUE)
    setButtonCustomization(primaryButtonCustomization, ButtonType.NEXT)

    val secondaryButtonCustomization = ButtonCustomization().apply {
        textColor = colors.primary.toHex()
        cornerRadius = attributes.cornerRadius
    }
    setButtonCustomization(secondaryButtonCustomization, ButtonType.CANCEL)
    setButtonCustomization(secondaryButtonCustomization, ButtonType.RESEND)
    setButtonCustomization(secondaryButtonCustomization, ButtonType.OPEN_OOB_APP)

    expandableInfoCustomization = ExpandableInfoCustomization().apply {
        textColor = colors.text.toHex()
        headingTextColor = colors.textSecondary.toHex()
        setExpandStateIndicatorColor(colors.primary.toHex())
        highlightedBackgroundColor = colors.containerOutline.toHex()
        borderColor = colors.outline.toHex()
    }
}
