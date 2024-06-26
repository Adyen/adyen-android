/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/1/2023.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.components.core.internal.BaseComponentCallback
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.core.PermissionHandlerCallback
import org.json.JSONObject

/**
 * Implement this callback to interact with a [PaymentComponent].
 */
interface ComponentCallback<T : PaymentComponentState<*>> : BaseComponentCallback {

    /**
     * In this method you should make a network call to the /payments endpoint of the Checkout API through your server.
     *
     * We provide a [PaymentComponentState] which contains information about the state of the payment component at the
     * moment the user submits the payment.
     *
     * We also provide inside [PaymentComponentState.data] the parameters that we can infer from the component's
     * configuration and the user input, especially the [state.data.paymentMethod] object with the shopper input
     * details.
     *
     * Use [PaymentComponentData.SERIALIZER] to serialize this data to a [JSONObject]. The rest of the /payments call
     * request data should be filled in, on your server, according to your needs.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param state The state of the payment component at the moment the user submits the payment.
     */
    fun onSubmit(state: T)

    /**
     * In this method you should make a network call to the /payments/details endpoint of the Checkout API through your
     * server.
     *
     * We provide inside [ActionComponentData] the whole request data expected by the /payments/details endpoint. Use
     * [ActionComponentData.SERIALIZER] to serialize this data to a [JSONObject].
     *
     * You can dismiss the component after this API call is successful, there is no need to perform any extra actions.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentData The data from the action component.
     */
    fun onAdditionalDetails(actionComponentData: ActionComponentData)

    /**
     * The component has encountered an error.
     * Use [ComponentError.exception] to get the internal exception.
     *
     * @param componentError The error encountered.
     */
    fun onError(componentError: ComponentError)

    /**
     * You can implement this optional method to receive an update any time the state of the component changes.
     *
     * This mainly occurs through a user interaction (editing an input, selecting an item from a list, etc) and will be
     * triggered regardless whether the component is valid or not.
     *
     * We provide a [PaymentComponentState] which contains information about the state of the payment component at that
     * moment.
     *
     * @param state The state of the payment component at the current moment.
     */
    fun onStateChanged(state: T) = Unit

    /**
     * Should be overridden to support runtime permissions for components.
     * Runtime permission should be requested and communicated back through the callback.
     * If not overridden, [PermissionHandlerCallback.onPermissionRequestNotHandled] will be invoked, which will show an
     * error message.
     *
     * @param requiredPermission Required runtime permission.
     * @param permissionCallback Callback to be used when passing permission result.
     */
    fun onPermissionRequest(requiredPermission: String, permissionCallback: PermissionHandlerCallback) {
        // To be optionally overridden
        permissionCallback.onPermissionRequestNotHandled(requiredPermission)
    }

    /**
     * Should be overridden for components that require additional availability checks, like Google Pay and WeChat pay.
     *
     * @param isAvailable If the payment method is available or not.
     */
    fun onAvailabilityResult(isAvailable: Boolean) = Unit
}
