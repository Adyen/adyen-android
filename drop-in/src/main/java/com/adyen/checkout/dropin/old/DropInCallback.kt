/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old

/**
 * A class that defines the callbacks from Drop-in to the Activity or Fragment that launched it.
 */
@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
fun interface DropInCallback {

    /**
     * Returns the final result of Drop-in.
     * Use this method together with [DropIn.registerForDropInResult].
     *
     * @param dropInResult The final result of Drop-in.
     */
    fun onDropInResult(dropInResult: DropInResult?)
}
