/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/11/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.state.format
import com.adyen.checkout.core.common.internal.helper.LocalLocale
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * The host's row for an address: it shows [address] on one line and calls [onClick], on which the host opens the
 * address form.
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
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview
@Composable
private fun AddressPickerFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressPickerField(
            address = AddressModel(
                country = "US",
                street = "Market Street 1",
                houseNumberOrName = "Apt 4",
                postalCode = "94107",
                city = "San Francisco",
                stateOrProvince = "CA",
            ),
            errorMessage = null,
            onClick = {},
        )

        AddressPickerField(
            address = null,
            // Any key works: the host passes its own error.
            errorMessage = CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR,
            onClick = {},
        )
    }
}
