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
import com.adyen.checkout.card.internal.ui.state.CardFieldId
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.CardNumberFormat
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.state.InstallmentViewState
import com.adyen.checkout.card.internal.ui.state.StorePaymentViewState
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

@Suppress("LongMethod")
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
        // The order decides both what is shown and in which order, so there is no configuration to read here. A field
        // is keyed on its id rather than its position, so that its text and focus follow it if the order changes.
        viewState.fieldOrder.forEach { fieldId ->
            key(fieldId) {
                CardField(
                    fieldId = fieldId,
                    viewState = viewState,
                    onIntent = onIntent,
                    onScanButtonClick = onScanButtonClick,
                    onInstallmentPickerClick = onInstallmentPickerClick,
                )
            }
        }
    }
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
private fun CardField(
    fieldId: CardFieldId,
    viewState: CardViewState,
    onIntent: (CardIntent) -> Unit,
    onScanButtonClick: () -> Unit,
    onInstallmentPickerClick: () -> Unit,
) {
    val onFocusChange: (Boolean) -> Unit = { hasFocus -> onIntent(CardIntent.UpdateFieldFocus(fieldId, hasFocus)) }
    val onFocusRequestConsumed: () -> Unit = { onIntent(CardIntent.FocusRequestConsumed(fieldId)) }

    // A field in the order whose view state is missing is skipped rather than crashing. CardFieldOrderTest is what
    // makes sure the two never disagree in the first place.
    when (fieldId) {
        CardFieldId.CARD_NUMBER -> viewState.cardNumber?.let { fieldViewState ->
            CardNumberField(
                cardNumberState = fieldViewState,
                supportedCardBrandsViewState = viewState.supportedCardBrandsViewState,
                cardBrandViewState = viewState.cardBrandViewState,
                cardNumberFormat = viewState.cardNumberFormat,
                onValueChange = { onIntent(CardIntent.UpdateCardNumber(it)) },
                onFocusChange = onFocusChange,
                onFocusRequestConsumed = onFocusRequestConsumed,
                onScanButtonClick = onScanButtonClick,
                onBrandSelect = { onIntent(CardIntent.SelectBrand(it)) },
            )
        }

        CardFieldId.EXPIRY_DATE -> viewState.expiryDate?.let { fieldViewState ->
            ExpiryDateField(
                expiryDateState = fieldViewState,
                onValueChange = { onIntent(CardIntent.UpdateExpiryDate(it)) },
                onFocusChange = onFocusChange,
                onFocusRequestConsumed = onFocusRequestConsumed,
            )
        }

        CardFieldId.SECURITY_CODE -> viewState.securityCode?.let { fieldViewState ->
            SecurityCodeField(
                securityCodeState = fieldViewState,
                cardNumberFormat = viewState.cardNumberFormat,
                onValueChange = { onIntent(CardIntent.UpdateSecurityCode(it)) },
                onFocusChange = onFocusChange,
                onFocusRequestConsumed = onFocusRequestConsumed,
            )
        }

        CardFieldId.HOLDER_NAME -> viewState.holderName?.let { fieldViewState ->
            HolderNameField(
                holderNameState = fieldViewState,
                onValueChange = { onIntent(CardIntent.UpdateHolderName(it)) },
                onFocusChange = onFocusChange,
                onFocusRequestConsumed = onFocusRequestConsumed,
            )
        }

        CardFieldId.SOCIAL_SECURITY_NUMBER -> viewState.socialSecurityNumber?.let { fieldViewState ->
            SocialSecurityNumberField(
                socialSecurityNumberState = fieldViewState,
                onValueChange = { onIntent(CardIntent.UpdateSocialSecurityNumber(it)) },
                onFocusChange = onFocusChange,
                onFocusRequestConsumed = onFocusRequestConsumed,
            )
        }

        CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER -> viewState.kcpBirthDateOrTaxNumber?.let { fieldViewState ->
            KCPBirthDateOrTaxNumberField(
                kcpBirthDateOrTaxNumberState = fieldViewState,
                onValueChange = { onIntent(CardIntent.UpdateKcpBirthDateOrTaxNumber(it)) },
                onFocusChange = onFocusChange,
                onFocusRequestConsumed = onFocusRequestConsumed,
            )
        }

        CardFieldId.KCP_CARD_PASSWORD -> viewState.kcpCardPassword?.let { fieldViewState ->
            KCPCardPasswordField(
                kcpCardPasswordState = fieldViewState,
                onValueChange = { onIntent(CardIntent.UpdateKcpCardPassword(it)) },
                onFocusChange = onFocusChange,
                onFocusRequestConsumed = onFocusRequestConsumed,
            )
        }

        CardFieldId.POSTAL_CODE -> viewState.postalCode?.let { fieldViewState ->
            PostalCodeField(
                postalCodeState = fieldViewState,
                onValueChange = { onIntent(CardIntent.UpdatePostalCode(it)) },
                onFocusChange = onFocusChange,
                onFocusRequestConsumed = onFocusRequestConsumed,
            )
        }

        CardFieldId.STORE_PAYMENT_METHOD -> viewState.storePaymentViewState?.let { fieldViewState ->
            SwitchContainer(
                checked = fieldViewState.isSelected,
                onCheckedChange = { onIntent(CardIntent.UpdateStorePaymentMethod(it)) },
            ) {
                Body(resolveString(CheckoutLocalizationKey.CARD_STORE_PAYMENT_METHOD))
            }
        }

        CardFieldId.INSTALLMENTS -> viewState.installmentViewState?.let { fieldViewState ->
            Subtitle(
                text = resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS),
                modifier = Modifier.padding(top = Dimensions.Spacing.Small),
            )
            ValuePickerField(
                value = fieldViewState.selectedInstallment?.toDisplayText() ?: "",
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
                fieldOrder = listOf(
                    CardFieldId.CARD_NUMBER,
                    CardFieldId.EXPIRY_DATE,
                    CardFieldId.SECURITY_CODE,
                    CardFieldId.STORE_PAYMENT_METHOD,
                ),
                cardNumber = TextInputViewState(
                    text = "5555444433331111",
                ),
                expiryDate = TextInputViewState(
                    text = "1234",
                ),
                securityCode = TextInputViewState(
                    text = "737",
                ),
                holderName = null,
                socialSecurityNumber = null,
                kcpBirthDateOrTaxNumber = null,
                kcpCardPassword = null,
                postalCode = null,
                storePaymentViewState = StorePaymentViewState(isSelected = true),

                supportedCardBrandsViewState = SupportedCardBrandsViewState(
                    supportedCardBrands = emptyList(),
                    isVisible = false,
                ),
                isLoading = false,
                isCardScanButtonVisible = false,
                cardBrandViewState = CardBrandViewState.SingleBrand(CardBrand(CardType.MASTERCARD.txVariant)),
                cardNumberFormat = CardNumberFormat.DEFAULT,
                installmentViewState = null,
                payButtonViewState = PayButtonViewState(null, false),
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
                fieldOrder = CardFieldId.entries,
                cardNumber = TextInputViewState(
                    text = "5555444433331111",
                ),
                expiryDate = TextInputViewState(
                    text = "1234",
                ),
                securityCode = TextInputViewState(
                    text = "737",
                ),
                holderName = TextInputViewState(
                    text = "J. Smith",
                ),
                socialSecurityNumber = TextInputViewState(
                    text = "12123123123412",
                ),
                kcpBirthDateOrTaxNumber = TextInputViewState(
                    text = "1234567890",
                ),
                kcpCardPassword = TextInputViewState(
                    text = "12",
                ),
                postalCode = TextInputViewState(
                    text = "1234 AB",
                ),
                storePaymentViewState = StorePaymentViewState(isSelected = true),

                supportedCardBrandsViewState = SupportedCardBrandsViewState(
                    supportedCardBrands = emptyList(),
                    isVisible = false,
                ),
                isLoading = false,
                isCardScanButtonVisible = false,
                cardBrandViewState = CardBrandViewState.SingleBrand(CardBrand(CardType.MASTERCARD.txVariant)),
                cardNumberFormat = CardNumberFormat.DEFAULT,
                installmentViewState = InstallmentViewState(
                    installmentOptions = listOf(InstallmentModel.OneTime),
                    selectedInstallment = InstallmentModel.OneTime,
                ),
                payButtonViewState = PayButtonViewState(null, false),
            ),
            onIntent = {},
            onSubmitClick = {},
            onScanButtonClick = {},
            onInstallmentPickerClick = {},
        )
    }
}
