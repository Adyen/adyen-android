package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.card.internal.ui.properties.PostalCodeProperties
import com.adyen.checkout.core.common.internal.ui.CheckoutTextFieldTrailingIcon
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.rememberTextFieldStateWithCurrentValue
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun PostalCodeField(
    postalCodeState: TextInputViewState,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingTextPostalCode = postalCodeState.supportingText?.let { resolveString(it) }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        label = resolveString(CheckoutLocalizationKey.CARD_POSTAL_CODE),
        state = rememberTextFieldStateWithCurrentValue(postalCodeState.text),
        inputTransformation = InputTransformation.maxLength(PostalCodeProperties.POSTAL_CODE_MAX_LENGTH),
        isError = postalCodeState.isError,
        supportingText = supportingTextPostalCode,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Unspecified,
        ),
        shouldFocus = postalCodeState.isFocused,
        trailingIcon = {
            CheckoutTextFieldTrailingIcon(postalCodeState.trailingIcon)
        },
    )
}

@Preview
@Composable
private fun PostalCodeFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        PostalCodeField(
            postalCodeState = TextInputViewState(
                text = "1234 AB",
            ),
            onFocusChange = {},
            onValueChange = {},
        )

        PostalCodeField(
            postalCodeState = TextInputViewState(
                text = "12",
                isError = true,
            ),
            onFocusChange = {},
            onValueChange = {},
        )
    }
}
