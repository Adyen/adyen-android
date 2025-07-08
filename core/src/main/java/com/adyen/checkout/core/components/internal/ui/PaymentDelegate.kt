/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.BaseComponentState

// TODO - Some delegates might not be composable,
//  Move ComposableDelegate to PaymentMethod specific delegate later
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PaymentDelegate<T : BaseComponentState> :
    ComposableDelegate,
    EventDelegate<T> {

    fun submit()
}
