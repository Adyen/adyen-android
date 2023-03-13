/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/12/2022.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo

/**
 * A component that requires a button to be clicked so that it can be submitted. This button might be visible during all
 * or part of the payment flow.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ButtonComponent {

    /**
     * Indicates whether the component requires user interaction before the payment flow can be triggered.
     * User interaction usually means filling an input, clicking a button, selecting an item from a list, etc.
     *
     * If no interaction is required, the component can be submitted at any point after it's loaded.
     */
    fun isConfirmationRequired(): Boolean

    /**
     * Submits the component manually. You can call this function to trigger a payment submission if the component is
     * ready to make the payment.
     *
     * If the component is valid your [onSubmit] callback method will be triggered. Otherwise, any invalid inputs will
     * be highlighted to bring them to the user's attention.
     */
    fun submit()
}
