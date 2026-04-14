/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/4/2026.
 */

package com.adyen.checkout.core.components

import android.annotation.SuppressLint

/**
 * Represents the different routes in the checkout flow.
 */
sealed class CheckoutRoute {

    /**
     * Route to display an action component. Use [CheckoutAction] to display the action.
     */
    @SuppressLint("ObjectInPublicSealedClass")
    data object Action : CheckoutRoute()

    /**
     * Route to display secondary content. Use [CheckoutSecondary] to display the secondary content.
     *
     * @param identifier The unique identifier for the secondary screen.
     */
    data class Secondary(val identifier: String) : CheckoutRoute()
}
