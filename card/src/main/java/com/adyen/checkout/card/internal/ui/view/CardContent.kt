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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.toDisplayText
import com.adyen.checkout.card.internal.ui.state.CardBrandViewState
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.CardNumberFormat
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.state.InstallmentViewState
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.SwitchContainer
import com.adyen.checkout.ui.internal.element.button.PayButton
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Subtitle
import com.adyen.checkout.ui.internal.theme.Dimensions
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
        footer = {
            PayButton(onClick = onSubmitClick, isLoading = viewState.isLoading)
        },
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
        if (viewState.cardNumber != null) {
            CardNumberField(
                cardNumberState = viewState.cardNumber,
                supportedCardBrands = viewState.supportedCardBrands,
                isSupportedCardBrandsShown = viewState.isSupportedCardBrandsShown,
                cardBrandViewState = viewState.cardBrandViewState,
                cardNumberFormat = viewState.cardNumberFormat,
                onValueChange = { onIntent(CardIntent.UpdateCardNumber(it)) },
                onFocusChange = { onIntent(CardIntent.UpdateCardNumberFocus(it)) },
                onScanButtonClick = onScanButtonClick,
                onBrandSelect = { onIntent(CardIntent.SelectBrand(it)) },
            )
        }
        if (viewState.expiryDate != null) {
            ExpiryDateField(
                expiryDateState = viewState.expiryDate,
                onValueChange = { onIntent(CardIntent.UpdateExpiryDate(it)) },
                onFocusChange = { onIntent(CardIntent.UpdateExpiryDateFocus(it)) },
            )
        }
        if (viewState.securityCode != null) {
            SecurityCodeField(
                securityCodeState = viewState.securityCode,
                cardNumberFormat = viewState.cardNumberFormat,
                onValueChange = { onIntent(CardIntent.UpdateSecurityCode(it)) },
                onFocusChange = { onIntent(CardIntent.UpdateSecurityCodeFocus(it)) },
            )
        }
        if (viewState.holderName != null) {
            HolderNameField(
                holderNameState = viewState.holderName,
                onValueChange = { onIntent(CardIntent.UpdateHolderName(it)) },
                onFocusChange = { onIntent(CardIntent.UpdateHolderNameFocus(it)) },
            )
        }
        if (viewState.socialSecurityNumber != null) {
            SocialSecurityNumberField(
                socialSecurityNumberState = viewState.socialSecurityNumber,
                onValueChange = { onIntent(CardIntent.UpdateSocialSecurityNumber(it)) },
                onFocusChange = { onIntent(CardIntent.UpdateSocialSecurityNumberFocus(it)) },
            )
        }
        if (viewState.kcpBirthDateOrTaxNumber != null) {
            KCPBirthDateOrTaxNumberField(
                kcpBirthDateOrTaxNumberState = viewState.kcpBirthDateOrTaxNumber,
                onValueChange = { onIntent(CardIntent.UpdateKcpBirthDateOrTaxNumber(it)) },
                onFocusChange = { onIntent(CardIntent.UpdateKcpBirthDateOrTaxNumberFocus(it)) },
            )
        }
        if (viewState.kcpCardPassword != null) {
            KCPCardPasswordField(
                kcpCardPasswordState = viewState.kcpCardPassword,
                onValueChange = { onIntent(CardIntent.UpdateKcpCardPassword(it)) },
                onFocusChange = { onIntent(CardIntent.UpdateKcpCardPasswordFocus(it)) },
            )
        }
        if (viewState.postalCode != null) {
            PostalCodeField(
                postalCodeState = viewState.postalCode,
                onFocusChange = { onIntent(CardIntent.UpdatePostalCodeFocus(it)) },
                onValueChange = { onIntent(CardIntent.UpdatePostalCode(it)) },
            )
        }
        if (viewState.isStorePaymentFieldVisible) {
            SwitchContainer(
                checked = viewState.storePaymentMethod,
                onCheckedChange = { onIntent(CardIntent.UpdateStorePaymentMethod(it)) },
            ) {
                Body(resolveString(CheckoutLocalizationKey.CARD_STORE_PAYMENT_METHOD))
            }
        }
        if (viewState.installmentViewState != null) {
            Subtitle(resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS))
            ValuePickerField(
                value = viewState.installmentViewState.selectedInstallment?.toDisplayText() ?: "",
                label = resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_TITLE),
                onClick = onInstallmentPickerClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CardContentPreview() {
    CardContent(
        viewState = CardViewState(
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
            storePaymentMethod = false,
            isStorePaymentFieldVisible = true,
            supportedCardBrands = emptyList(),
            isSupportedCardBrandsShown = false,
            isLoading = false,
            isCardScanButtonVisible = false,
            cardBrandViewState = CardBrandViewState.SingleBrand(CardBrand(CardType.MASTERCARD.txVariant)),
            cardNumberFormat = CardNumberFormat.DEFAULT,
            installmentViewState = InstallmentViewState(
                installmentOptions = listOf(InstallmentModel.OneTime),
                selectedInstallment = InstallmentModel.OneTime,
            ),
        ),
        onIntent = {},
        onSubmitClick = {},
        onScanButtonClick = {},
        onInstallmentPickerClick = {},
    )
}
