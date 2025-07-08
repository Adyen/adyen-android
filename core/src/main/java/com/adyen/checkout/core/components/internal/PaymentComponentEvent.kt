/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class PaymentComponentEvent<ComponentStateT : BaseComponentState> {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    class Submit<ComponentStateT : BaseComponentState>(
        val state: ComponentStateT
    ) : PaymentComponentEvent<ComponentStateT>()
}
