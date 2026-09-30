/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/9/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.model.format
import com.adyen.checkout.core.common.internal.helper.LocalLocale
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * The row on the host's form that summarizes the address and opens the address form when clicked.
 *
 * @param address The address the host keeps, or null while there is none.
 * @param errorMessage The error to show, or null while there is none or it is held back.
 * @param onClick Called when the row is clicked. The host then calls
 * [com.adyen.checkout.address.internal.ui.AddressComponent.show].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun AddressPickerField(
    address: AddressModel?,
    errorMessage: CheckoutLocalizationKey?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ValuePickerField(
        value = address?.format(LocalLocale.current).orEmpty(),
        label = resolveString(CheckoutLocalizationKey.ADDRESS_LABEL),
        onClick = onClick,
        supportingText = errorMessage?.let { resolveString(it) },
        isError = errorMessage != null,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun AddressPickerFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressPickerField(
            address = AddressModel(country = "NL", postalCode = "1234 AB"),
            errorMessage = null,
            onClick = {},
        )
    }
}
