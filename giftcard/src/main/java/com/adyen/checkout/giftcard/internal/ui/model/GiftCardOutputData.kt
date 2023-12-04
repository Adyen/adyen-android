/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/9/2021.
 */

package com.adyen.checkout.giftcard.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData

internal data class GiftCardOutputData(
    val numberFieldState: FieldState<String>,
    val pinFieldState: FieldState<String>,
) : OutputData {

    override val isValid: Boolean
        get() = numberFieldState.validation.isValid() && pinFieldState.validation.isValid()
}
