/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by robertsc on 15/6/2026.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.components.data.BeforeSubmitData

/**
 * The result of the [SessionCheckoutCallbacks.onBeforeSubmit] callback.
 *
 * Return [Proceed] to continue the session submission, or [Abort] to stop it. Aborting the submission does not trigger
 * the error callback.
 */
sealed interface BeforeSubmitResult {

    /**
     * Continue the submission flow with the provided data.
     *
     * Use [data] to return shopper data unchanged or with modified fields. Null fields in [BeforeSubmitData] preserve
     * the values collected by the component.
     *
     * When you patch the session on your server, pass the returned [sessionData] so the SDK can use the updated session
     * state for the following sessions `/payments` request. Leave it null when the session was not patched.
     *
     * @param data The shopper data to continue with.
     * @param sessionData The session data returned by your server after patching the session, if any.
     */
    class Proceed(
        val data: BeforeSubmitData,
        val sessionData: String? = null,
    ) : BeforeSubmitResult

    /**
     * Stop the submission flow and reset the component to the ready state.
     *
     * This does not call the error callback.
     */
    class Abort : BeforeSubmitResult
}
