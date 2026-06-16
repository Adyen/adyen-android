package com.adyen.checkout.card.internal.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.card.internal.ui.model.PostalCodeTrailingIcon
import com.adyen.checkout.card.internal.ui.properties.PostalCodeProperties
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.TextFieldStylePreviewParameterProvider
import com.adyen.checkout.ui.internal.element.input.rememberTextFieldStateWithCurrentValue
import com.adyen.checkout.ui.internal.helper.CheckoutThemeWrapper
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
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
            PostalCodeIcon(state = postalCodeState)
        },
    )
}

@Composable
private fun PostalCodeIcon(
    state: TextInputViewState,
    modifier: Modifier = Modifier,
) {
    val resourceId: Int?
    val tint: Color
    when (state.trailingIcon as? PostalCodeTrailingIcon) {
        PostalCodeTrailingIcon.Warning -> {
            resourceId = com.adyen.checkout.test.R.drawable.ic_warning
            tint = CheckoutThemeProvider.colors.destructive
        }

        else -> {
            resourceId = null
            tint = Color.Unspecified
        }
    }

    AnimatedContent(
        targetState = resourceId to tint,
        modifier = modifier,
        label = "PostalCodeIcon",
    ) { (targetResourceId, targetTint) ->
        if (targetResourceId != null) {
            Icon(
                modifier = Modifier.size(Dimensions.LogoSize.small),
                imageVector = ImageVector.vectorResource(targetResourceId),
                contentDescription = null,
                tint = targetTint,
            )
        }
    }
}

@Preview
@Composable
private fun PostalCodeFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemeWrapper(theme) {
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
                trailingIcon = PostalCodeTrailingIcon.Warning,
            ),
            onFocusChange = {},
            onValueChange = {},
        )
    }
}
