/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2022.
 */

package com.adyen.checkout.ui.core.internal.ui

import androidx.annotation.RestrictTo
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface UIStateDelegate {

    val uiStateFlow: Flow<PaymentComponentUIState>

    // TODO: This can be removed when all validation logic is moved out of the views
    val uiEventFlow: Flow<PaymentComponentUIEvent>
}
