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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.helper.getThemedIcon
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun ExpiryDateField(
    expiryDateState: TextInputComponentState,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showExpiryDateError =
        expiryDateState.errorMessage != null && expiryDateState.showError
    val supportingTextExpiryDate = if (showExpiryDateError) {
        expiryDateState.errorMessage?.let { resolveString(it) }
    } else {
        resolveString(CheckoutLocalizationKey.CARD_EXPIRY_DATE_HINT)
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(CardIntent.UpdateExpiryDateFocus(focusState.isFocused))
            },
        label = resolveString(CheckoutLocalizationKey.CARD_EXPIRY_DATE),
        initialValue = expiryDateState.text,
        isError = showExpiryDateError,
        supportingText = supportingTextExpiryDate,
        onValueChange = { value ->
            onIntent(CardIntent.UpdateExpiryDate(value))
        },
        inputTransformation = ExpiryDateInputTransformation()
            .maxLength(maxLength = ExpiryDateInputTransformation.MAX_DIGITS),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shouldFocus = expiryDateState.isFocused,
        trailingIcon = {
            ExpiryDateIcon(expiryDateState)
        },
    )
}

@Composable
private fun ExpiryDateIcon(
    state: TextInputComponentState,
    modifier: Modifier = Modifier,
) {
    val isValid = state.errorMessage == null
    val isInvalid = state.errorMessage != null && state.showError

    val resourceId = when {
        isValid -> com.adyen.checkout.test.R.drawable.ic_checkmark
        isInvalid -> com.adyen.checkout.test.R.drawable.ic_warning
        else -> getThemedIcon(
            backgroundColor = CheckoutThemeProvider.elements.textField.backgroundColor,
            lightDrawableId = R.drawable.ic_card_expiry_date_light,
            darkDrawableId = R.drawable.ic_card_expiry_date_dark,
        )
    }

    AnimatedContent(
        targetState = resourceId,
        modifier = modifier,
        label = "ExpiryDateIcon",
    ) { targetResourceId ->
        val iconSize = remember(isInvalid) {
            if (isInvalid) {
                Dimensions.LogoSize.smallSquare
            } else {
                Dimensions.LogoSize.small
            }
        }

        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = ImageVector.vectorResource(targetResourceId),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}
