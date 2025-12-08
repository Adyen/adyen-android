/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element.button

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.BodyEmphasized
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.internal.theme.toCompose
import com.adyen.checkout.ui.theme.CheckoutTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun CheckoutButtonGroup(
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        modifier = modifier,
    ) {
        val style = CheckoutThemeProvider.elements.buttonGroup
        val toggleButtonColors = remember(style) {
            ToggleButtonColors(
                contentColor = style.uncheckedTextColor,
                containerColor = style.uncheckedContainerColor,
                checkedContentColor = style.checkedTextColor,
                checkedContainerColor = style.checkedContainerColor,
                disabledContentColor = style.disabledContentColor,
                disabledContainerColor = style.disabledContainerColor,
            )
        }
        items.forEachIndexed { index, item ->
            CheckoutToggleButton(
                text = item,
                checked = index == selectedIndex,
                onCheckedChange = {
                    selectedIndex = index
                    onItemSelected(index)
                },
                colors = toggleButtonColors,
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    items.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CheckoutToggleButton(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: ToggleButtonColors,
    shapes: ToggleButtonShapes,
    modifier: Modifier = Modifier,
) {
    ToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = colors,
        shapes = shapes,
        modifier = modifier.semantics { role = Role.RadioButton },
    ) {
        if (checked) {
            BodyEmphasized(text, color = LocalContentColor.current)
        } else {
            Body(text, color = LocalContentColor.current)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckoutButtonGroupPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            Modifier
                .background(theme.colors.background.toCompose())
                .padding(Dimensions.Large),
        ) {
            CheckoutButtonGroup(
                items = listOf("Item 1", "Item 2", "Item 3"),
                onItemSelected = {},
            )
        }
    }
}
