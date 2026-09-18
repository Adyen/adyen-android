/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.card

import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.FillableData
import androidx.compose.ui.autofill.createFromText
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.adyen.checkout.card.internal.ui.state.CardBrandViewState
import com.adyen.checkout.card.internal.ui.state.CardNumberFormat
import com.adyen.checkout.card.internal.ui.state.SupportedCardBrandsViewState
import com.adyen.checkout.card.internal.ui.view.CardNumberField
import com.adyen.checkout.card.internal.ui.view.ExpiryDateField
import com.adyen.checkout.card.internal.ui.view.HolderNameField
import com.adyen.checkout.card.internal.ui.view.PostalCodeField
import com.adyen.checkout.card.internal.ui.view.SecurityCodeField
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.theme.CheckoutTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
class CardAutofillTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenApplicableCardFieldsAreRendered_thenAutofillContentTypesAreExposed() {
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                CardNumberField(
                    cardNumberState = TextInputViewState(),
                    supportedCardBrandsViewState = SupportedCardBrandsViewState(emptyList(), isVisible = false),
                    cardBrandViewState = CardBrandViewState.Placeholder,
                    cardNumberFormat = CardNumberFormat.DEFAULT,
                    onValueChange = {},
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                    onScanButtonClick = {},
                    onBrandSelect = {},
                )
                ExpiryDateField(
                    expiryDateState = TextInputViewState(),
                    onValueChange = {},
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
                SecurityCodeField(
                    securityCodeState = TextInputViewState(),
                    cardNumberFormat = CardNumberFormat.DEFAULT,
                    onValueChange = {},
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
                HolderNameField(
                    holderNameState = TextInputViewState(),
                    onValueChange = {},
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
                PostalCodeField(
                    postalCodeState = TextInputViewState(),
                    onValueChange = {},
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
            }
        }

        listOf(
            ContentType.CreditCardNumber,
            ContentType.CreditCardExpirationDate,
            ContentType.CreditCardSecurityCode,
            ContentType.PersonFullName,
            ContentType.PostalCode,
        ).forEach { contentType ->
            composeTestRule.onNode(hasContentType(contentType), useUnmergedTree = true).assertExists()
        }
    }

    @Test
    fun whenCardDetailsAreAutofilled_thenTransformedValuesReachCallbacks() {
        var cardNumber by mutableStateOf("")
        var expiryDate by mutableStateOf("")
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                CardNumberField(
                    cardNumberState = TextInputViewState(text = cardNumber),
                    supportedCardBrandsViewState = SupportedCardBrandsViewState(emptyList(), isVisible = false),
                    cardBrandViewState = CardBrandViewState.Placeholder,
                    cardNumberFormat = CardNumberFormat.DEFAULT,
                    onValueChange = { cardNumber = it },
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                    onScanButtonClick = {},
                    onBrandSelect = {},
                )
                ExpiryDateField(
                    expiryDateState = TextInputViewState(text = expiryDate),
                    onValueChange = { expiryDate = it },
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
            }
        }

        performAutofill(ContentType.CreditCardNumber, FORMATTED_CARD_NUMBER)
        performAutofill(ContentType.CreditCardExpirationDate, FORMATTED_EXPIRY_DATE)
        composeTestRule.waitUntil {
            cardNumber == CARD_NUMBER && expiryDate == EXPIRY_DATE
        }

        assertEquals(CARD_NUMBER, cardNumber)
        assertEquals(EXPIRY_DATE, expiryDate)
    }

    private fun performAutofill(contentType: ContentType, value: String) {
        val fillableData = checkNotNull(FillableData.createFromText(value))
        composeTestRule.onNode(hasContentType(contentType), useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnFillData) { action ->
                action(fillableData)
            }
    }

    private fun hasContentType(contentType: ContentType) =
        SemanticsMatcher.expectValue(SemanticsProperties.ContentType, contentType)

    private companion object {
        const val FORMATTED_CARD_NUMBER = "4000 6200 0000 0007"
        const val CARD_NUMBER = "4000620000000007"
        const val FORMATTED_EXPIRY_DATE = "12/30"
        const val EXPIRY_DATE = "1230"
    }
}
