/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */
package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails

// TODO sessions: docs
/**
 * A component that handles collecting user input data. It handles validating and formatting the data for the UI.
 * A valid [PaymentComponentState] contains [PaymentMethodDetails] to help compose the payments/ call on the backend.
 *
 *
 *
 * Should be used attached to a corresponding ComponentView to get data from.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PaymentComponent : Component {

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
