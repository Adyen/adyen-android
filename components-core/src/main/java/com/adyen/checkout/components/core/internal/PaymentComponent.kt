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

/**
 * A component that handles collecting user input data. It handles validating and formatting the data for the UI.
 * A valid [PaymentComponentState] contains [PaymentMethodDetails] to help compose the request to the /payments API
 * call.
 *
 * This component can also handle additional actions.
 *
 * Can be attached to [AdyenComponentView] to present a view to the user.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PaymentComponent : Component {

    /**
     * Sets whether the user is allowed to interact with the component or not.
     */
    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
