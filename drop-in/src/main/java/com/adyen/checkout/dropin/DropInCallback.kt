/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 2/8/2021.
 */

package com.adyen.checkout.dropin

/**
 * A class that defines the callbacks from Drop-in to the component that launched it.
 */
interface DropInCallback {

    /**
     * Returns the final result of Drop-in.
     * Use this method together with [DropIn.registerForDropInResult].
     *
     * @param dropInResult The final result of Drop-in.
     */
    fun onDropInResult(dropInResult: DropInResult?)
}
