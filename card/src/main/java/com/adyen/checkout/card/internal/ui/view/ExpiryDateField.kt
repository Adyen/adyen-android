/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 11/11/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.ui.model.ExpiryDateTrailingIcon
import com.adyen.checkout.core.common.internal.properties.ExpiryDateProperties
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.SeparatorsOutputTransformation
import com.adyen.checkout.ui.internal.element.input.TextFieldSeparator
import com.adyen.checkout.ui.internal.element.input.TextFieldStylePreviewParameterProvider
import com.adyen.checkout.ui.internal.element.input.rememberTextFieldStateWithCurrentValue
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.getThemedIcon
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun ExpiryDateField(
    expiryDateState: TextInputViewState,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingTextExpiryDate = expiryDateState.supportingText?.let { resolveString(it) }
        ?: resolveString(CheckoutLocalizationKey.CARD_EXPIRY_DATE_HINT)

    val labelSuffix = if (expiryDateState.isOptional) {
        " ${resolveString(CheckoutLocalizationKey.GENERAL_OPTIONAL)}"
    } else {
        ""
    }

    val inputTransformation = remember { ExpiryDateInputTransformation() }
    val outputTransformation = remember {
        SeparatorsOutputTransformation(
            listOf(
                TextFieldSeparator(ExpiryDateProperties.EXPIRY_DATE_SEPARATOR, indexInRawString = 2),
            ),
        )
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        label = resolveString(key = CheckoutLocalizationKey.CARD_EXPIRY_DATE) + labelSuffix,
        state = rememberTextFieldStateWithCurrentValue(expiryDateState.text),
        isError = expiryDateState.isError,
        supportingText = supportingTextExpiryDate,
        onValueChange = onValueChange,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        shouldFocus = expiryDateState.isFocused,
        trailingIcon = {
            ExpiryDateIcon(expiryDateState)
        },
    )
}

@Composable
private fun ExpiryDateIcon(
    state: TextInputViewState,
    modifier: Modifier = Modifier,
) {
    val trailingIcon = state.trailingIcon as? ExpiryDateTrailingIcon

    val resourceId: Int
    val tint: Color
    when (trailingIcon) {
        ExpiryDateTrailingIcon.Checkmark -> {
            resourceId = com.adyen.checkout.test.R.drawable.ic_checkmark
            tint = CheckoutThemeProvider.colors.primary
        }

        ExpiryDateTrailingIcon.Warning -> {
            resourceId = com.adyen.checkout.test.R.drawable.ic_warning
            tint = CheckoutThemeProvider.colors.destructive
        }

        else -> {
            resourceId = getThemedIcon(
                backgroundColor = CheckoutThemeProvider.elements.textField.backgroundColor,
                lightDrawableId = R.drawable.ic_card_expiry_date_light,
                darkDrawableId = R.drawable.ic_card_expiry_date_dark,
            )
            tint = Color.Unspecified
        }
    }

    AnimatedContent(
        targetState = resourceId to tint,
        modifier = modifier,
        label = "ExpiryDateIcon",
    ) { (targetResourceId, targetTint) ->
        Icon(
            modifier = Modifier.size(Dimensions.LogoSize.small),
            imageVector = ImageVector.vectorResource(targetResourceId),
            contentDescription = null,
            tint = targetTint,
        )
    }
}

@Preview
@Composable
private fun ExpiryDateFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        ExpiryDateField(
            expiryDateState = TextInputViewState(
                text = "0330",
            ),
            onValueChange = {},
            onFocusChange = {},
        )
        ExpiryDateField(
            expiryDateState = TextInputViewState(
                isOptional = true,
            ),
            onValueChange = {},
            onFocusChange = {},
        )

        ExpiryDateField(
            expiryDateState = TextInputViewState(
                isError = true,
                trailingIcon = ExpiryDateTrailingIcon.Warning,
            ),
            onValueChange = {},
            onFocusChange = {},
        )
    }
}
