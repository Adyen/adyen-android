/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.action.data.Action as ActionResponse

// TODO - KDocs, revisit later after having parameters
/**
 * Represents the outcome of a checkout operation.
 */
sealed interface CheckoutResult {

    /** Indicates the payment process has finished successfully. */
    class Finished : CheckoutResult

    /** Indicates that an additional action is required from the shopper. */
    class Action(val action: ActionResponse) : CheckoutResult

    /** Indicates an error occurred during the payment process. */
    class Error : CheckoutResult
}
