/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 11/12/2025.
 */

package com.adyen.checkout.card.internal.ui.helper

import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState

internal fun CVCVisibility.toInputFieldUIState() = when (this) {
    CVCVisibility.ALWAYS_SHOW -> InputFieldUIState.REQUIRED
    CVCVisibility.HIDE_FIRST -> InputFieldUIState.HIDDEN
    CVCVisibility.ALWAYS_HIDE -> InputFieldUIState.HIDDEN
}
