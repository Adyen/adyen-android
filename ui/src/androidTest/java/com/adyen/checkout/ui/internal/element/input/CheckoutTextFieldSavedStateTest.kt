/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.ui.internal.element.input

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.theme.CheckoutTheme
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckoutTextFieldSavedStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Restoring the state and checking the field would not catch a regression, because the field is reset to the
    // current value after a restore either way. The saved values are therefore checked directly.
    @Test
    fun whenTextIsEntered_thenItIsNotWrittenToTheSavedState() {
        val registry = SaveableStateRegistry(restoredValues = null) { true }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                CheckoutThemePreviewWrapper(CheckoutTheme()) {
                    CheckoutTextField(
                        state = rememberTextFieldStateWithCurrentValue(""),
                        label = LABEL,
                        contentType = null,
                        modifier = Modifier.testTag(TEXT_FIELD_TAG),
                        trailingContent = null,
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput(SENSITIVE_TEXT)

        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG, useUnmergedTree = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(SENSITIVE_TEXT)))
        val savedValues = composeTestRule.runOnIdle { registry.performSave() }
        assertFalse(savedValues.toString().contains(SENSITIVE_TEXT))
    }

    private companion object {
        const val LABEL = "Card number"
        const val SENSITIVE_TEXT = "4111111111111111"
        const val TEXT_FIELD_TAG = "text_field"
    }
}
