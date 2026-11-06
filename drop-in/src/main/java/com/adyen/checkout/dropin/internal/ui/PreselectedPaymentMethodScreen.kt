/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scrim
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.adyen.checkout.core.common.internal.ui.IconHeaderContent
import com.adyen.checkout.core.common.internal.ui.NavigationCloseButton
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.element.button.SecondaryButton
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.launch

@Composable
internal fun PreselectedPaymentMethodScreen(
    viewModel: PreselectedPaymentMethodViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    viewState?.let {
        PreselectedPaymentMethodBottomSheet(onDismissed = { viewModel.onBackClicked() }) { dismiss ->
            PreselectedPaymentMethodContent(
                it,
                onBackClicked = dismiss,
                onPayClicked = { viewModel.onPayClicked() },
                onOtherPaymentMethodClicked = { viewModel.onOtherPaymentMethodClicked() },
            )
        }
    }
}

/**
 * Renders the sheet in the composition rather than in a window of its own, so it takes part in the transitions and the
 * predictive back animations of the screens around it.
 *
 * @param onDismissed Called once the sheet is fully hidden.
 * @param content Receives a callback that hides the sheet before calling [onDismissed].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreselectedPaymentMethodBottomSheet(
    onDismissed: () -> Unit,
    content: @Composable (dismiss: () -> Unit) -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    // NavDisplay only resumes this entry once no screen is animating over it, or away from it. Until then, the sheet
    // cannot be dismissed, so a back press, swipe or click while the next screen slides in does not hide the sheet
    // underneath it.
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = {
        if (isResumed) {
            scope.launch {
                sheetState.hide()
                onDismissed()
            }
        }
    }

    // The state is saveable, so coming back to this screen restores it expanded and this does not animate again
    LaunchedEffect(sheetState) { sheetState.show() }

    val scrimAlpha by animateFloatAsState(if (sheetState.targetValue == SheetValue.Hidden) 0f else 1f)

    Box(Modifier.fillMaxSize()) {
        Scrim(
            contentDescription = resolveString(CheckoutLocalizationKey.GENERAL_CLOSE),
            onClick = dismiss,
            alpha = { scrimAlpha },
        )

        // Aligned to the top, because the sheet offsets itself from the top of the available height. Padded, so a sheet
        // taller than the screen stops below the status bar rather than going under it.
        BottomSheet(
            state = sheetState,
            onDismissRequest = onDismissed,
            gesturesEnabled = isResumed,
            backHandlerEnabled = isResumed,
            dragHandle = null,
            containerColor = CheckoutThemeProvider.colors.background,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        ) {
            Spacer(Modifier.size(Dimensions.Spacing.Small))
            content(dismiss)
            Spacer(Modifier.size(Dimensions.Spacing.Large))
        }
    }
}

@Composable
private fun PreselectedPaymentMethodContent(
    viewState: PreselectedPaymentMethodViewState,
    onBackClicked: () -> Unit,
    onPayClicked: () -> Unit,
    onOtherPaymentMethodClicked: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        NavigationCloseButton(
            onClick = onBackClicked,
        )

        Spacer(Modifier.size(Dimensions.Spacing.ExtraLarge))

        IconHeaderContent(
            logoTxVariant = viewState.logoTxVariant,
            title = viewState.title,
            description = viewState.subtitle.resolve(),
            modifier = Modifier.padding(horizontal = Dimensions.Spacing.Large),
        ) {
            PrimaryButton(
                onClick = onPayClicked,
                text = viewState.payButtonText.resolve(),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.size(Dimensions.Spacing.Large))

            SecondaryButton(
                onClick = onOtherPaymentMethodClicked,
                text = resolveString(CheckoutLocalizationKey.DROP_IN_OTHER_PAYMENT_METHODS),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreselectedPaymentMethodScreenPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    PreselectedPaymentMethodContentPreview(
        theme = theme,
        subtitle = UiText.Localized(
            CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION_WITH_AMOUNT,
            listOf("Visa", "$9.99"),
        ),
        payButtonText = UiText.Localized(
            CheckoutLocalizationKey.DROPIN_PRESELECTED_PAYMENT_METHOD_BUTTON_LABEL,
            listOf("Visa"),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun PreselectedPaymentMethodScreenZeroAmountPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    PreselectedPaymentMethodContentPreview(
        theme = theme,
        subtitle = UiText.Localized(
            CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION_SAVE_DETAILS,
            listOf("Visa"),
        ),
        payButtonText = UiText.Localized(CheckoutLocalizationKey.PAY_BUTTON_ZERO_AMOUNT),
    )
}

@Preview(showBackground = true)
@Composable
private fun PreselectedPaymentMethodScreenNoAmountPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    PreselectedPaymentMethodContentPreview(
        theme = theme,
        subtitle = UiText.Localized(CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION, listOf("Visa")),
        payButtonText = UiText.Localized(
            CheckoutLocalizationKey.DROPIN_PRESELECTED_PAYMENT_METHOD_BUTTON_LABEL,
            listOf("Visa"),
        ),
    )
}

@Composable
private fun PreselectedPaymentMethodContentPreview(
    theme: CheckoutTheme,
    subtitle: UiText,
    payButtonText: UiText,
) {
    CheckoutThemePreviewWrapper(theme) {
        val viewState = PreselectedPaymentMethodViewState(
            logoTxVariant = "visa",
            title = "•••• 1234",
            subtitle = subtitle,
            payButtonText = payButtonText,
        )
        PreselectedPaymentMethodContent(
            viewState = viewState,
            onBackClicked = {},
            onPayClicked = {},
            onOtherPaymentMethodClicked = {},
        )
    }
}
