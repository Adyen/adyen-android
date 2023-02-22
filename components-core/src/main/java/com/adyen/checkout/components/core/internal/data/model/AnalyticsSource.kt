/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/11/2022.
 */

package com.adyen.checkout.components.core.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class AnalyticsSource {

    class DropIn : AnalyticsSource()

    class PaymentComponent internal constructor(
        internal val isCreatedByDropIn: Boolean,
        internal val paymentMethodType: String
    ) : AnalyticsSource() {
        constructor(isCreatedByDropIn: Boolean, paymentMethod: PaymentMethod) : this(
            isCreatedByDropIn,
            paymentMethod.type.orEmpty()
        )

        constructor(isCreatedByDropIn: Boolean, storedPaymentMethod: StoredPaymentMethod) : this(
            isCreatedByDropIn,
            storedPaymentMethod.type.orEmpty()
        )
    }
}
