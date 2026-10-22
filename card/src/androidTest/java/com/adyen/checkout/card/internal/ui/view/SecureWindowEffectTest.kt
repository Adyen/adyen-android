/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.card.internal.ui.view

import android.app.Activity
import android.view.View
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureWindowEffectTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenShownInActivity_thenActivityWindowIsSecure() {
        var activity: Activity? = null
        composeTestRule.setContent {
            activity = LocalActivity.current
            SecureWindowEffect(isEnabled = true)
        }

        composeTestRule.waitUntil("activity window is secure", TIMEOUT_MS) { activity?.window.isSecure() }
    }

    @Test
    fun whenShownInDialog_thenDialogWindowIsSecure() {
        var dialogWindow: Window? = null
        composeTestRule.setContent {
            Dialog(onDismissRequest = {}) {
                dialogWindow = LocalView.current.dialogWindow()
                SecureWindowEffect(isEnabled = true)
            }
        }

        composeTestRule.waitUntil("dialog window is secure", TIMEOUT_MS) { dialogWindow.isSecure() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun whenShownInModalBottomSheet_thenBottomSheetWindowIsSecure() {
        var sheetWindow: Window? = null
        composeTestRule.setContent {
            ModalBottomSheet(onDismissRequest = {}) {
                sheetWindow = LocalView.current.dialogWindow()
                SecureWindowEffect(isEnabled = true)
            }
        }

        composeTestRule.waitUntil("bottom sheet window is secure", TIMEOUT_MS) { sheetWindow.isSecure() }
    }

    @Test
    fun whenEffectLeavesComposition_thenDialogWindowIsNoLongerSecure() {
        var isEffectShown by mutableStateOf(true)
        var dialogWindow: Window? = null
        composeTestRule.setContent {
            Dialog(onDismissRequest = {}) {
                dialogWindow = LocalView.current.dialogWindow()
                if (isEffectShown) SecureWindowEffect(isEnabled = true)
            }
        }
        composeTestRule.waitUntil("dialog window is secure", TIMEOUT_MS) { dialogWindow.isSecure() }

        composeTestRule.runOnIdle { isEffectShown = false }

        composeTestRule.waitUntil("dialog window is not secure", TIMEOUT_MS) { !dialogWindow.isSecure() }
    }

    @Test
    fun whenDialogHostRecomposes_thenDialogWindowStaysSecure() {
        var dismissOnClickOutside by mutableStateOf(true)
        var dialogWindow: Window? = null
        composeTestRule.setContent {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnClickOutside = dismissOnClickOutside),
            ) {
                dialogWindow = LocalView.current.dialogWindow()
                SecureWindowEffect(isEnabled = true)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle { dismissOnClickOutside = false }

        composeTestRule.waitUntil("dialog window is secure after recomposition", TIMEOUT_MS) {
            dialogWindow.isSecure()
        }
    }

    private fun Window?.isSecure() = this != null && attributes.flags and FLAG_SECURE != 0

    private fun View.dialogWindow(): Window = generateSequence(parent) { it.parent }
        .filterIsInstance<DialogWindowProvider>()
        .first()
        .window

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
