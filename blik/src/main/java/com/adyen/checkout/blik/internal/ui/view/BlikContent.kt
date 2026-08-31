/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.blik.internal.ui.state.BlikFormElement
import com.adyen.checkout.blik.internal.ui.state.BlikIntent
import com.adyen.checkout.blik.internal.ui.state.BlikViewState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.payButtonAsComponentScaffoldFooter
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun BlikContent(
    viewStateFlow: StateFlow<BlikViewState>,
    onSubmitClick: () -> Unit,
    onIntent: (BlikIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewState by viewStateFlow.collectAsStateWithLifecycle()
    BlikContent(
        viewState = viewState,
        onSubmitClick = onSubmitClick,
        onIntent = onIntent,
        modifier = modifier,
    )
}

@Composable
private fun BlikContent(
    viewState: BlikViewState,
    onSubmitClick: () -> Unit,
    onIntent: (BlikIntent) -> Unit,
    modifier: Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        disableInteraction = viewState.isLoading,
        footer = payButtonAsComponentScaffoldFooter(viewState.payButtonViewState, onSubmitClick),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
        ) {
            // Static screen copy rather than a form field, so it is not one of the elements below.
            Body(text = resolveString(CheckoutLocalizationKey.BLIK_HELPER_TEXT))

            viewState.elements.forEach { element ->
                key(element.id) {
                    BlikFormElementContent(element = element, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun BlikFormElementContent(
    element: BlikFormElement,
    onIntent: (BlikIntent) -> Unit,
) {
    when (element) {
        is BlikFormElement.BlikCode -> BlikCodeField(
            blikCodeState = element.textInputViewState,
            onValueChange = { onIntent(BlikIntent.UpdateBlikCode(it)) },
            onFocusChange = { onIntent(BlikIntent.UpdateFieldFocus(element.id, it)) },
            onFocusRequestConsumed = { onIntent(BlikIntent.FocusRequestConsumed(element.id)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlikContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        BlikContent(
            viewState = BlikViewState(
                elements = listOf(BlikFormElement.BlikCode(TextInputViewState())),
                isLoading = false,
                payButtonViewState = PayButtonViewState(null, false),
            ),
            onIntent = {},
            onSubmitClick = {},
            modifier = Modifier,
        )
    }
}
