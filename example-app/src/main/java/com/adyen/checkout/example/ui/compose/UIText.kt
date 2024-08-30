/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 28/8/2024.
 */

package com.adyen.checkout.example.ui.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Can be used for texts that need to be displayed on the UI and can be either a string or a string resource.
 */
internal sealed interface UIText {
    data class String(val value: kotlin.String) : UIText
    class Resource(@StringRes val stringResId: Int, vararg val formatArgs: Any) : UIText

}

@Composable
internal fun stringFromUIText(uiText: UIText): String {
    return when (uiText) {
        is UIText.Resource -> stringResource(id = uiText.stringResId, formatArgs = uiText.formatArgs)
        is UIText.String -> uiText.value
    }
}
