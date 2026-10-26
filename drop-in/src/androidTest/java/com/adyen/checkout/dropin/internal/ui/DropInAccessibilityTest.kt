/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.theme.CheckoutTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class DropInAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenTopAppBarHasTitle_thenTitleIsExposedAsHeading() {
        composeTestRule.setContent {
            CheckoutThemePreviewWrapper(CheckoutTheme()) {
                DropInTopAppBar(
                    title = TITLE,
                    navigationIcon = {},
                    scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
                )
            }
        }

        composeTestRule.onAllNodesWithText(TITLE).onFirst()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    private companion object {
        const val TITLE = "Cards"
    }
}
