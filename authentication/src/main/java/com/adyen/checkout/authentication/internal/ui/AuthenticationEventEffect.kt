/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/12/2025.
 */

package com.adyen.checkout.authentication.internal.ui

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.flow.Flow

@Composable
internal fun AuthenticationEventEffect(
    handleAction: (Activity, UiCustomization) -> Unit,
    viewEventFlow: Flow<AuthenticationEvent>,
    onError: (InternalCheckoutError) -> Unit,
) {
    val activity = LocalActivity.current
    val colors = CheckoutThemeProvider.colors
    val attributes = CheckoutThemeProvider.attributes
    val uiCustomization = remember(colors, attributes) { mapToUiCustomization(colors, attributes) }

    LaunchedEffect(viewEventFlow, activity, uiCustomization) {
        viewEventFlow.collect { event ->
            when (event) {
                is AuthenticationEvent.HandleAction -> {
                    if (activity == null) {
                        onError(GenericError("Activity is not available."))
                    } else {
                        handleAction(activity, uiCustomization)
                    }
                }
            }
        }
    }
}
