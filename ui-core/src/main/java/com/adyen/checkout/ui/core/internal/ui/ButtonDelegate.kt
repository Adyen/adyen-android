package com.adyen.checkout.ui.core.internal.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ButtonDelegate {

    fun onSubmit()

    /**
     * Indicates whether the component requires user interaction before the payment flow can be triggered.
     * User interaction usually means filling an input, clicking a button, selecting an item from a list, etc.
     *
     * If no interaction is required, the component can be submitted at any point after it's loaded.
     */
    fun isConfirmationRequired(): Boolean

    fun shouldShowSubmitButton(): Boolean
}
