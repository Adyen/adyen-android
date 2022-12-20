/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/12/2022.
 */

package com.adyen.checkout.components

interface ButtonComponent {

    /**
     * Tells if the view interaction requires confirmation from the user to start the payment flow.
     * Confirmation usually is obtained by a "Pay" button the user need to press to start processing the payment.
     * If confirmation is not required, it means the view handles input in a way that the user has already expressed the
     * desire to continue.
     *
     * Each type of view always returns the same value, so if the type of view is known, there is no need to check this
     * method.
     *
     * @return If an update from the component attached to this View requires further user confirmation to continue or
     * not.
     */
    fun isConfirmationRequired(): Boolean
}
