/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.card

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.state.CardFormElement
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.view.CardContent
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenInstallmentsAreShown_thenTheirTitleIsExposedAsHeading() {
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                CardContent(
                    modifier = Modifier,
                    viewStateFlow = MutableStateFlow(
                        CardViewState(
                            elements = listOf(CardFormElement.Installments(InstallmentModel.OneTime)),
                            isLoading = false,
                            payButtonViewState = null,
                            installmentPickerViewState = null,
                        ),
                    ),
                    onIntent = {},
                    onSubmitClick = {},
                    onInstallmentPickerClick = {},
                    initializeCardScanner = {},
                    onCardScannerResult = { _, _ -> },
                    onScanButtonClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(INSTALLMENTS_TITLE)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    private companion object {
        const val INSTALLMENTS_TITLE = "Number of installments"
    }
}
