/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.address

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
import com.adyen.checkout.address.internal.ui.view.AddressCityField
import com.adyen.checkout.address.internal.ui.view.AddressHouseNumberField
import com.adyen.checkout.address.internal.ui.view.AddressPostalCodeField
import com.adyen.checkout.address.internal.ui.view.AddressStreetField
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.theme.CheckoutTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
class AddressAutofillTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenAddressFieldsAreRendered_thenAutofillContentTypesAreExposed() {
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                AddressStreetField(
                    label = CheckoutLocalizationKey.ADDRESS_STREET_LABEL,
                    streetState = TextInputViewState(),
                    onValueChange = {},
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
                AddressHouseNumberField(
                    label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL,
                    houseNumberState = TextInputViewState(),
                    onValueChange = {},
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
                AddressPostalCodeField(
                    label = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL,
                    maxLength = MAX_POSTAL_CODE_LENGTH,
                    postalCodeState = TextInputViewState(),
                    onValueChange = {},
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
                AddressCityField(
                    label = CheckoutLocalizationKey.ADDRESS_CITY_LABEL,
                    cityState = TextInputViewState(),
                    onValueChange = {},
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
            }
        }

        listOf(
            ContentType.AddressStreet,
            ContentType.AddressAuxiliaryDetails,
            ContentType.PostalCode,
            ContentType.AddressLocality,
        ).forEach { contentType ->
            composeTestRule.onNode(hasContentType(contentType), useUnmergedTree = true).assertExists()
        }
    }

    @Test
    fun whenStreetIsAutofilledOnTwoLines_thenTheLineBreakReachesTheCallbackAsASpace() {
        var street by mutableStateOf("")
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                AddressStreetField(
                    label = CheckoutLocalizationKey.ADDRESS_STREET_LABEL,
                    streetState = TextInputViewState(text = street),
                    onValueChange = { street = it },
                    onFocusChange = {},
                    onFocusRequestConsumed = {},
                )
            }
        }

        val fillableData = checkNotNull(FillableData.createFromText(TWO_LINE_STREET))
        composeTestRule.onNode(hasContentType(ContentType.AddressStreet), useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnFillData) { action ->
                action(fillableData)
            }
        composeTestRule.waitUntil { street == ONE_LINE_STREET }

        assertEquals(ONE_LINE_STREET, street)
    }

    private fun hasContentType(contentType: ContentType) =
        SemanticsMatcher.expectValue(SemanticsProperties.ContentType, contentType)

    private companion object {
        const val MAX_POSTAL_CODE_LENGTH = 10
        const val TWO_LINE_STREET = "Main Street 1\nApt 4"
        const val ONE_LINE_STREET = "Main Street 1 Apt 4"
    }
}
