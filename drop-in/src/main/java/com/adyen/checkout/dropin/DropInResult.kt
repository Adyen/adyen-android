/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/2/2021.
 */

package com.adyen.checkout.dropin

import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult

/**
 * A class that contains the final result of Drop-in.
 */
sealed class DropInResult {

    /**
     * Drop-in was dismissed by the user before it has completed.
     */
    class CancelledByUser : DropInResult()

    /**
     * Drop-in has encountered an error.
     *
     * Two scenarios could trigger this result:
     * - An exception occurred during Drop-in.
     * - [DropInServiceResult.Error] was returned in [DropInService]. In this case, the [reason]
     * parameter will have the same value as [DropInServiceResult.Error.reason].
     *
     * @param reason The reason of the error.
     */
    class Error(val reason: String?) : DropInResult()

    /**
     * Drop-in has completed.
     * This occurs after returning [DropInServiceResult.Finished] in the [DropInService].
     *
     * @param result The result of Drop-in, mirrors the value of [DropInServiceResult.Finished.result].
     */
    class Finished(val result: String) : DropInResult()
}
