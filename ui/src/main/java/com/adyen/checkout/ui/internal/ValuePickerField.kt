/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/6/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.theme.AdyenCheckoutTheme as Theme

/**
 * A composable that displays a value and a label, designed to trigger an action on click.
 * This field is not directly editable by the user and is visually consistent with [AdyenTextField].
 * It's typically used for scenarios where selecting a value requires opening a separate picker or dialog.
 *
 * @param value The current value to be displayed in the field.
 * @param label The descriptive label for the field.
 * @param onClick Lambda to be executed when the field is clicked.
 * @param modifier Optional [Modifier] to be applied to this composable.
 * @param supportingText Optional text displayed below the field for additional context or guidance.
 * @param isError Boolean indicating whether the field should be displayed in an error state.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun ValuePickerField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    val style = AdyenTextFieldDefaults.textFieldStyle(AdyenCheckoutTheme.elements.textField)
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    AdyenTextField(
        value = value,
        onValueChange = {},
        label = label,
        // This makes sure the whole composable is clickable, but the ripple is not displayed outside of the inner field
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        ),
        enabled = false,
        supportingText = supportingText,
        isError = isError,
        trailingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = style.textColor,
            )
        },
        interactionSource = interactionSource,
        innerIndication = ripple(color = style.textColor),
    )
}

@Preview
@Composable
private fun ValuePickerFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: Theme,
) {
    AdyenCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
            modifier = Modifier
                .background(theme.colors.background.toCompose())
                .padding(Dimensions.Large),
        ) {
            ValuePickerField(
                value = "Value",
                label = "Label",
                onClick = { },
                supportingText = "Description",
            )

            ValuePickerField(
                value = "Value",
                label = "Label",
                onClick = { },
                supportingText = "Description",
                isError = true,
            )
        }
    }
}
