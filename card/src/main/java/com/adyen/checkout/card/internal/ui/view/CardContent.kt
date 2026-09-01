/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.toDisplayText
import com.adyen.checkout.card.internal.ui.state.CardBrandViewState
import com.adyen.checkout.card.internal.ui.state.CardFormElement
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.CardNumberFormat
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.state.SupportedCardBrandsViewState
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.payButtonAsComponentScaffoldFooter
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.SwitchContainer
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Subtitle
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun CardContent(
    modifier: Modifier,
    viewStateFlow: StateFlow<CardViewState>,
    onIntent: (CardIntent) -> Unit,
    onSubmitClick: () -> Unit,
    onInstallmentPickerClick: () -> Unit,
    initializeCardScanner: (Context) -> Unit,
    onCardScannerResult: (Int, Intent?) -> Unit,
    onScanButtonClick: (ActivityResultLauncher<IntentSenderRequest>) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        initializeCardScanner(context)
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        onCardScannerResult(result.resultCode, result.data)
        // Re-initialize to get a fresh PendingIntent, as Google's PaymentCardRecognitionPendingIntent is single-use
        initializeCardScanner(context)
    }

    val viewState by viewStateFlow.collectAsStateWithLifecycle()
    CardContent(
        viewState = viewState,
        onIntent = onIntent,
        onSubmitClick = onSubmitClick,
        onScanButtonClick = {
            onScanButtonClick(scannerLauncher)
        },
        onInstallmentPickerClick = onInstallmentPickerClick,
        modifier = modifier,
    )
}

@Composable
private fun CardContent(
    viewState: CardViewState,
    onIntent: (CardIntent) -> Unit,
    onSubmitClick: () -> Unit,
    onScanButtonClick: () -> Unit,
    onInstallmentPickerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        disableInteraction = viewState.isLoading,
        footer = payButtonAsComponentScaffoldFooter(viewState.payButtonViewState, onSubmitClick),
    ) {
        CardDetailsSection(
            viewState = viewState,
            onIntent = onIntent,
            onScanButtonClick = onScanButtonClick,
            onInstallmentPickerClick = onInstallmentPickerClick,
        )
    }
}

@Composable
private fun CardDetailsSection(
    viewState: CardViewState,
    onIntent: (CardIntent) -> Unit,
    onScanButtonClick: () -> Unit,
    onInstallmentPickerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
    ) {
        // Each element carries its own data, so there is no configuration and no other view state to read here. An
        // element is keyed on its id rather than its position, so that its text and focus follow it if the order
        // changes.
        viewState.elements.forEach { element ->
            key(element.id) {
                CardFormElementContent(
                    element = element,
                    onIntent = onIntent,
                    onScanButtonClick = onScanButtonClick,
                    onInstallmentPickerClick = onInstallmentPickerClick,
                )
            }
        }
    }
}

// Field composables take the element's data rather than the element itself, so that they stay usable from more than one
// component. The security code field is shared with the stored card screen, and the billing address will be shared more
// widely still.
@Suppress("LongMethod")
@Composable
private fun CardFormElementContent(
    element: CardFormElement,
    onIntent: (CardIntent) -> Unit,
    onScanButtonClick: () -> Unit,
    onInstallmentPickerClick: () -> Unit,
) {
    val onFocusChange: (Boolean) -> Unit = { hasFocus -> onIntent(CardIntent.UpdateFieldFocus(element.id, hasFocus)) }
    val onFocusRequestConsumed: () -> Unit = { onIntent(CardIntent.FocusRequestConsumed(element.id)) }

    when (element) {
        is CardFormElement.CardNumber -> CardNumberField(
            cardNumberState = element.textInputViewState,
            supportedCardBrandsViewState = element.supportedCardBrandsViewState,
            cardBrandViewState = element.cardBrandViewState,
            cardNumberFormat = element.cardNumberFormat,
            onValueChange = { onIntent(CardIntent.UpdateCardNumber(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
            onScanButtonClick = onScanButtonClick,
            onBrandSelect = { onIntent(CardIntent.SelectBrand(it)) },
        )

        is CardFormElement.ExpiryDate -> ExpiryDateField(
            expiryDateState = element.textInputViewState,
            onValueChange = { onIntent(CardIntent.UpdateExpiryDate(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is CardFormElement.SecurityCode -> SecurityCodeField(
            securityCodeState = element.textInputViewState,
            cardNumberFormat = element.cardNumberFormat,
            onValueChange = { onIntent(CardIntent.UpdateSecurityCode(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is CardFormElement.HolderName -> HolderNameField(
            holderNameState = element.textInputViewState,
            onValueChange = { onIntent(CardIntent.UpdateHolderName(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is CardFormElement.SocialSecurityNumber -> SocialSecurityNumberField(
            socialSecurityNumberState = element.textInputViewState,
            onValueChange = { onIntent(CardIntent.UpdateSocialSecurityNumber(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is CardFormElement.KcpBirthDateOrTaxNumber -> KCPBirthDateOrTaxNumberField(
            kcpBirthDateOrTaxNumberState = element.textInputViewState,
            onValueChange = { onIntent(CardIntent.UpdateKcpBirthDateOrTaxNumber(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is CardFormElement.KcpCardPassword -> KCPCardPasswordField(
            kcpCardPasswordState = element.textInputViewState,
            onValueChange = { onIntent(CardIntent.UpdateKcpCardPassword(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is CardFormElement.PostalCode -> PostalCodeField(
            postalCodeState = element.textInputViewState,
            onValueChange = { onIntent(CardIntent.UpdatePostalCode(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is CardFormElement.StorePaymentMethod -> SwitchContainer(
            checked = element.isSelected,
            onCheckedChange = { onIntent(CardIntent.UpdateStorePaymentMethod(it)) },
        ) {
            Body(resolveString(CheckoutLocalizationKey.CARD_STORE_PAYMENT_METHOD))
        }

        is CardFormElement.Installments -> {
            Subtitle(
                text = resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS),
                modifier = Modifier.padding(top = Dimensions.Spacing.Small),
            )
            ValuePickerField(
                value = element.selectedInstallment?.toDisplayText() ?: "",
                label = resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_TITLE),
                onClick = onInstallmentPickerClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CardContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        CardContent(
            viewState = CardViewState(
                elements = listOf(
                    previewCardNumber(),
                    CardFormElement.ExpiryDate(TextInputViewState(text = "1234")),
                    CardFormElement.SecurityCode(TextInputViewState(text = "737"), CardNumberFormat.DEFAULT),
                    CardFormElement.StorePaymentMethod(isSelected = true),
                ),
                isLoading = false,
                payButtonViewState = PayButtonViewState(null, false),
                installmentPickerViewState = null,
            ),
            onIntent = {},
            onSubmitClick = {},
            onScanButtonClick = {},
            onInstallmentPickerClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun CardContentPreviewAllFields(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        CardContent(
            viewState = CardViewState(
                elements = listOf(
                    previewCardNumber(),
                    CardFormElement.ExpiryDate(TextInputViewState(text = "1234")),
                    CardFormElement.SecurityCode(TextInputViewState(text = "737"), CardNumberFormat.DEFAULT),
                    CardFormElement.HolderName(TextInputViewState(text = "J. Smith")),
                    CardFormElement.SocialSecurityNumber(TextInputViewState(text = "12123123123412")),
                    CardFormElement.KcpBirthDateOrTaxNumber(TextInputViewState(text = "1234567890")),
                    CardFormElement.KcpCardPassword(TextInputViewState(text = "12")),
                    CardFormElement.PostalCode(TextInputViewState(text = "1234 AB")),
                    CardFormElement.StorePaymentMethod(isSelected = true),
                    CardFormElement.Installments(selectedInstallment = InstallmentModel.OneTime),
                ),
                isLoading = false,
                payButtonViewState = PayButtonViewState(null, false),
                installmentPickerViewState = null,
            ),
            onIntent = {},
            onSubmitClick = {},
            onScanButtonClick = {},
            onInstallmentPickerClick = {},
        )
    }
}

private fun previewCardNumber() = CardFormElement.CardNumber(
    textInputViewState = TextInputViewState(text = "5555444433331111"),
    cardBrandViewState = CardBrandViewState.SingleBrand(CardBrand(CardType.MASTERCARD.txVariant)),
    cardNumberFormat = CardNumberFormat.DEFAULT,
    supportedCardBrandsViewState = SupportedCardBrandsViewState(supportedCardBrands = emptyList(), isVisible = false),
)
