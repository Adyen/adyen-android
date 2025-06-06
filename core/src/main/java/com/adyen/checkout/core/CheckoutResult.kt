/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core

// TODO - KDocs, revisit later after having parameters
/**
 * Represents the outcome of a checkout operation.
 */
sealed interface CheckoutResult {

    /**
     * Represents advanced flow results that require further action or indicate a final state.
     */
    sealed interface Advanced : CheckoutResult {

        /** Indicates the payment process has finished successfully. */
        class Finished : Advanced

        /** Indicates that an additional action is required from the shopper. */
        class Action : Advanced

        /** Indicates an error occurred during the payment process. */
        class Error : Advanced
    }

    /**
     * Represents a result specific to the Sessions flow, typically indicating the SDK will handle the next steps.
     */
    class Sessions : CheckoutResult
}
