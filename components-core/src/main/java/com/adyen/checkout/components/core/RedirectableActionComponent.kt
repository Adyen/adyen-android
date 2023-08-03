/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/8/2023.
 */

package com.adyen.checkout.components.core

interface RedirectableActionComponent {

    /**
     * Set a callback that will be called when a redirect is made.
     *
     * @param listener The callback that will be called on redirect.
     */
    fun setOnRedirectListener(listener: () -> Unit)
}
