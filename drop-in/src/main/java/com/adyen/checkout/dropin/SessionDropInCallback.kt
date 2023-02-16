/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/2/2023.
 */

package com.adyen.checkout.dropin

/**
 * A class that defines the callbacks from Drop-in to the Activity or Fragment that launched it.
 */
fun interface SessionDropInCallback {

    /**
     * Returns the final result of Drop-in.
     * Use this method together with [DropIn.registerForDropInResult].
     *
     * @param sessionDropInResult The final result of Drop-in.
     */
    fun onDropInResult(sessionDropInResult: SessionDropInResult?)
}
