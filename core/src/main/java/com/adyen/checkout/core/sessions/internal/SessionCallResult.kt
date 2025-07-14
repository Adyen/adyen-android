/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.core.sessions.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.sessions.SessionPaymentResult
import com.adyen.checkout.core.action.data.Action as ActionResponse

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal interface SessionCallResult {

    sealed class Payments : SessionCallResult {
        data class Finished(val result: SessionPaymentResult) : Payments()
        data class Action(val action: ActionResponse) : Payments()
        data class Error(val throwable: Throwable) : Payments()
    }
}
