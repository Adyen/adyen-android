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
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.card.internal.ui.state.CardNumberFormat
import com.adyen.checkout.card.internal.ui.state.StoredCardFormElement
import com.adyen.checkout.card.internal.ui.state.StoredCardIntent
import com.adyen.checkout.card.internal.ui.state.StoredCardViewState
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
        // A stored card that asks for no security code has an empty form, and then there is no content to show at all.
        content = viewState.elements.takeIf { it.isNotEmpty() }?.let { elements ->
            @Composable {
                StoredCardForm(
                    elements = elements,
                    onIntent = onIntent,
                )
            }
        },
    )
}

@Composable
private fun StoredCardForm(
    elements: List<StoredCardFormElement>,
    onIntent: (StoredCardIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraLarge),
    ) {
        elements.forEach { element ->
            key(element.id) {
                StoredCardFormElementContent(element = element, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun StoredCardFormElementContent(
    element: StoredCardFormElement,
    onIntent: (StoredCardIntent) -> Unit,
) {
    when (element) {
        is StoredCardFormElement.SecurityCode -> SecurityCodeField(
            securityCodeState = element.textInputViewState,
            cardNumberFormat = element.cardNumberFormat,
            onValueChange = { onIntent(StoredCardIntent.UpdateSecurityCode(it)) },
            onFocusChange = { onIntent(StoredCardIntent.UpdateFieldFocus(element.id, it)) },
            onFocusRequestConsumed = { onIntent(StoredCardIntent.FocusRequestConsumed(element.id)) },
        )
    }
}

@Preview
@Composable
private fun StoredCardContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        StoredCardContent(
            viewState = StoredCardViewState(
                elements = listOf(
                    StoredCardFormElement.SecurityCode(
                        textInputViewState = TextInputViewState(
                            customTrailingIcon = SecurityCodeTrailingIcon.PlaceholderDefault,
                        ),
                        cardNumberFormat = CardNumberFormat.DEFAULT,
                    ),
                ),
                isLoading = false,
                payButtonViewState = PayButtonViewState(null, false),
            ),
            onIntent = {},
            onSubmitClick = {},
            modifier = Modifier,
        )
    }
}

@Preview
@Composable
private fun StoredCardContentWithoutSecurityCodePreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        StoredCardContent(
            viewState = StoredCardViewState(
                elements = emptyList(),
                isLoading = false,
                payButtonViewState = PayButtonViewState(null, false),
            ),
            onIntent = {},
            onSubmitClick = {},
            modifier = Modifier,
        )
    }
}
