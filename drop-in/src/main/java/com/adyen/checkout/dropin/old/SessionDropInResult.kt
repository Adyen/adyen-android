/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old

import com.adyen.checkout.sessions.core.SessionPaymentResult

/**
 * A class that contains the final result of Drop-in.
 */
sealed class SessionDropInResult {

    /**
     * Drop-in was dismissed by the user before it has completed.
     */
    class CancelledByUser : SessionDropInResult()

    /**
     * Drop-in has encountered an error.
     *
     * Two scenarios could trigger this result:
     * - An exception occurred during Drop-in.
     * - [DropInServiceResult.Error] was returned in your implementation of [SessionDropInService]. In this case, the
     * [reason] parameter will have the same value as [DropInServiceResult.Error.reason].
     *
     * @param reason The reason of the error.
     */
    class Error(val reason: String?) : SessionDropInResult()

    /**
     * Drop-in has completed.
     * This occurs after the payment is finished.
     *
     * @param result The result of the payment.
     */
    class Finished(val result: SessionPaymentResult) : SessionDropInResult()
}
