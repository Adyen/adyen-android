/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateIntent

internal sealed interface BlikIntent : ComponentStateIntent {

    data class UpdateBlikCode(val code: String) : BlikIntent

    data class UpdateBlikCodeFocus(val hasFocus: Boolean) : BlikIntent

    data class UpdateLoading(val isLoading: Boolean) : BlikIntent

    data object HighlightValidationErrors : BlikIntent
}
