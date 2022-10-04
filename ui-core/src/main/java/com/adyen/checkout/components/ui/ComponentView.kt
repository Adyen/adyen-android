/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2022.
 */
package com.adyen.checkout.components.ui

import android.content.Context
import android.view.View
import com.adyen.checkout.components.base.ComponentDelegate
import kotlinx.coroutines.CoroutineScope

/**
 * A View that can display input and fill in details for a Component.
 */
interface ComponentView {
    /**
     * This function will be called when the component is attached and the view is ready to get initialized.
     */
    fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context)

    /**
     * Tells if the view interaction requires confirmation from the user to start the payment flow.
     * Confirmation usually is obtained by a "Pay" button the user need to press to start processing the payment.
     * If confirmation is not required, it means the view handles input in a way that the user has already expressed the desire to continue.
     *
     * Each type of view always returns the same value, so if the type of view is known, there is no need to check this method.
     *
     * @return If an update from the component attached to this View requires further user confirmation to continue or not.
     */
    val isConfirmationRequired: Boolean

    /**
     * Highlight and focus on the current validation errors for the user to take action.
     * If the component doesn't need validation or if everything is already valid, nothing will happen.
     */
    fun highlightValidationErrors()

    fun getView(): View
}
