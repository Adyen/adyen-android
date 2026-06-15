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
 */
abstract class BeforeSubmitResult internal constructor() {

    /**
     * Continue the submission flow with the provided data.
     *
     * @param data The data to continue with.
     * @param sessionData The patched session data, if the session was updated.
     */
    class Proceed(
        val data: BeforeSubmitData,
        val sessionData: String? = null,
    ) : BeforeSubmitResult()

    /**
     * Stop the submission flow.
     */
    class Abort : BeforeSubmitResult()
}
