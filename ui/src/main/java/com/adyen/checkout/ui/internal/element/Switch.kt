/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme
import androidx.compose.material3.Switch as MaterialSwitch
import androidx.compose.material3.SwitchDefaults as MaterialSwitchDefaults

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SwitchContainer(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CheckoutThemeProvider.colors.container,
                shape = RoundedCornerShape(CheckoutThemeProvider.attributes.cornerRadius.dp),
            )
            .padding(horizontal = Dimensions.Medium, vertical = Dimensions.Small),
    ) {
        Box(Modifier.weight(1f)) {
            content()
        }
        Spacer(Modifier.size(Dimensions.Small))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val style = SwitchDefaults.switchStyle()
    MaterialSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = MaterialSwitchDefaults.colors(
            checkedThumbColor = style.checkedHandleColor,
            checkedTrackColor = style.checkedTrackColor,
            uncheckedThumbColor = style.uncheckedHandleColor,
            uncheckedTrackColor = style.uncheckedTrackColor,
            uncheckedBorderColor = style.uncheckedHandleColor,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SwitchContainerPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
            modifier = Modifier
                .background(CheckoutThemeProvider.colors.background)
                .padding(Dimensions.Large),
        ) {
            val description = "A very long and detailed description that covers multiple lines"
            SwitchContainer(
                checked = false,
                onCheckedChange = null,
            ) {
                Body(description)
            }

            SwitchContainer(
                checked = true,
                onCheckedChange = null,
            ) {
                Body(description)
            }
        }
    }
}
