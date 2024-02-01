/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/1/2023.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.components.core.internal.ActionComponent
import org.json.JSONObject

/**
 * Implement this callback to interact with an [ActionComponent].
 */
interface ActionComponentCallback {

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
     * The component requests runtime permission.
     * Runtime permission should be requested and communicated back through the callback.
     *
     * @param permissionRequestData Permission request data which contains requested permission and a callback.
     */
    fun onPermissionRequest(permissionRequestData: PermissionRequestData)

    /**
     * The component has encountered an error.
     * Use [ComponentError.exception] to get the internal exception.
     *
     * @param componentError The error encountered.
     */
    fun onError(componentError: ComponentError)
}
