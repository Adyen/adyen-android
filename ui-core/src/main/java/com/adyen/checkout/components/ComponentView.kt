/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/9/2019.
 */
package com.adyen.checkout.components

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.base.OutputData

/**
 * A View that can display input and fill in details for a PaymentComponent.
 *
 * @param <ComponentT> The [PaymentComponent] this view is able to interact with.
 */
interface ComponentView<OutputDataT : OutputData?, ComponentT : ViewableComponent<*, *, *>> {
    /**
     * Attach the [PaymentComponent] to the view to interact with.
     *
     * @param component      The component.
     * @param lifecycleOwner The lifecycle owner where the view is.
     */
    fun attach(component: ComponentT, lifecycleOwner: LifecycleOwner)

    /**
     * This function will be called when the component got attached to the View.
     * It's better to init ViewLess objects like ImageLoader here.
     */
    fun onComponentAttached()

    /**
     * This function will be called when the component is attached and the view is ready to get initialized.
     * It's better to find sub views here and add listeners to inputs changes or make the view Visible or Gone depends on the configuration of a
     * component.
     */
    fun initView()

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
}
