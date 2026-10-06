/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/10/2026.
 */

package com.adyen.checkout.core.components.internal

import android.os.Parcelable
import com.adyen.checkout.core.action.data.Action
import kotlinx.parcelize.Parcelize

/**
 * The phase a checkout flow is in, saved so that the flow can be restored after process death.
 */
internal sealed interface CheckoutFlowPhase : Parcelable {

    /**
     * The shopper can submit the payment.
     */
    @Parcelize
    data object Input : CheckoutFlowPhase

    /**
     * The payment was submitted and no response has been received yet.
     */
    @Parcelize
    data object Submitted : CheckoutFlowPhase

    /**
     * The flow is handling [action].
     */
    @Parcelize
    data class HandlingAction(val action: Action) : CheckoutFlowPhase
}
