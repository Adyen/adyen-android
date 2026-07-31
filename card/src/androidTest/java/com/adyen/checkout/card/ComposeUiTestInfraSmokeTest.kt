/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.card

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test proving the Compose instrumented test infrastructure (`androidTest` source set,
 * `ui-test-junit4` / `ui-test-manifest`) works in the `card` module. No production code is
 * exercised here on purpose - this only verifies the setup, not component behaviour.
 */
@RunWith(AndroidJUnit4::class)
class ComposeUiTestInfraSmokeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenComposableIsSet_thenItIsDisplayed() {
        composeTestRule.setContent {
            BasicText(text = "Compose UI test infra", modifier = Modifier.testTag(TEST_TAG))
        }

        composeTestRule.onNodeWithTag(TEST_TAG).assertIsDisplayed()
    }

    private companion object {
        const val TEST_TAG = "smoke_test_tag"
    }
}
