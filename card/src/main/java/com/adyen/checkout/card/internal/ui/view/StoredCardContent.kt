/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.card.internal.ui.state.CardNumberFormat
import com.adyen.checkout.card.internal.ui.state.StoredCardIntent
import com.adyen.checkout.card.internal.ui.state.StoredCardViewState
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.payButtonAsComponentScaffoldFooter
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun StoredCardContent(
    viewStateFlow: StateFlow<StoredCardViewState>,
    onIntent: (StoredCardIntent) -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewState by viewStateFlow.collectAsStateWithLifecycle()
    StoredCardContent(
        viewState = viewState,
        onIntent = onIntent,
        onSubmitClick = onSubmitClick,
        modifier = modifier,
    )
}

@Composable
private fun StoredCardContent(
    viewState: StoredCardViewState,
    onIntent: (StoredCardIntent) -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        disableInteraction = viewState.isLoading,
        footer = payButtonAsComponentScaffoldFooter(viewState.payButtonViewState, onSubmitClick),
        // If security code is not displayed, we should not display any content
        content = viewState.securityCode?.let { securityCodeState ->
            @Composable {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraLarge),
                ) {
                    SecurityCodeField(
                        securityCodeState = securityCodeState,
                        cardNumberFormat = viewState.cardNumberFormat,
                        onValueChange = { onIntent(StoredCardIntent.UpdateSecurityCode(it)) },
                        onFocusChange = { onIntent(StoredCardIntent.UpdateSecurityCodeFocus(it)) },
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun StoredCardContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        val viewState = StoredCardViewState(
            securityCode = TextInputViewState(
                customTrailingIcon = SecurityCodeTrailingIcon.PlaceholderDefault,
            ),
            brand = CardBrand(""),
            cardNumberFormat = CardNumberFormat.DEFAULT,
            isLoading = false,
            payButtonViewState = PayButtonViewState(null, false),
        )

        StoredCardContent(
            viewState = viewState,
            onIntent = {},
            onSubmitClick = {},
            modifier = Modifier,
        )
    }
}
