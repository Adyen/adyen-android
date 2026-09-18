/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.card

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.theme.CheckoutTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenTextFieldHasVisibleError_thenLocalizedErrorSemanticsAreExposed() {
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                CheckoutTextField(
                    label = LABEL,
                    state = rememberTextFieldState(),
                    contentType = null,
                    supportingText = ERROR_MESSAGE,
                    isError = true,
                    modifier = Modifier.testTag(TEXT_FIELD_TAG),
                    trailingIcon = null,
                )
            }
        }

        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG, useUnmergedTree = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, ERROR_MESSAGE))
    }

    @Test
    fun whenTextFieldHasOrdinarySupportingText_thenErrorSemanticsAreNotExposed() {
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                CheckoutTextField(
                    label = LABEL,
                    state = rememberTextFieldState(),
                    contentType = null,
                    supportingText = DESCRIPTION,
                    isError = false,
                    modifier = Modifier.testTag(TEXT_FIELD_TAG),
                    trailingIcon = null,
                )
            }
        }

        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG, useUnmergedTree = true)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
    }

    @Test
    fun whenSecureTextFieldHasVisibleError_thenPasswordAndErrorSemanticsAreExposed() {
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                CheckoutTextField(
                    label = LABEL,
                    state = rememberTextFieldState(SECRET_VALUE),
                    contentType = null,
                    supportingText = ERROR_MESSAGE,
                    isError = true,
                    isSecureField = true,
                    modifier = Modifier.testTag(TEXT_FIELD_TAG),
                    trailingIcon = null,
                )
            }
        }

        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG, useUnmergedTree = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, ERROR_MESSAGE))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
    }

    @Test
    fun whenValuePickerHasVisibleError_thenLocalizedErrorSemanticsAreExposed() {
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                ValuePickerField(
                    value = VALUE,
                    label = LABEL,
                    onClick = {},
                    modifier = Modifier.testTag(VALUE_PICKER_TAG),
                    supportingText = ERROR_MESSAGE,
                    isError = true,
                )
            }
        }

        composeTestRule.onNodeWithTag(VALUE_PICKER_TAG, useUnmergedTree = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, ERROR_MESSAGE))
    }

    private companion object {
        const val TEXT_FIELD_TAG = "text_field"
        const val VALUE_PICKER_TAG = "value_picker"
        const val LABEL = "Label"
        const val VALUE = "Value"
        const val DESCRIPTION = "Description"
        const val ERROR_MESSAGE = "Localized error message"
        const val SECRET_VALUE = "12"
    }
}
