/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/7/2023.
 */

package com.adyen.checkout.dropin.internal.provider

import com.adyen.checkout.card.CardComponentCallback
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.internal.ui.ExtraComponentCallbacks

inline fun <reified T : ComponentCallback<*>, C : PaymentComponentState<*>> getComponentCallback(
    baseCallback: ComponentCallback<C>,
    extraComponentCallbacks: ExtraComponentCallbacks?,
): T {
    Logger.d("TAG", "Type: ${T::class.simpleName}")
    val callback = when (T::class) {
        CardComponentCallback::class -> object : CardComponentCallback {
            override fun onSubmit(state: CardComponentState) {
                @Suppress("UNCHECKED_CAST")
                baseCallback.onSubmit(state as C)
            }

            override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
                baseCallback.onAdditionalDetails(actionComponentData)
            }

            override fun onError(componentError: ComponentError) {
                baseCallback.onError(componentError)
            }

            override fun onStateChanged(state: CardComponentState) {
                @Suppress("UNCHECKED_CAST")
                baseCallback.onStateChanged(state as C)
            }

            override fun onBinLookup(type: String, brands: List<String>) {
                extraComponentCallbacks?.onBinLookup(type, brands)
            }

            override fun onBinValue() {
                extraComponentCallbacks?.onBinValue()
            }
        }

        else -> baseCallback
    }

    Logger.d("TAG", "Resolved type: ${callback::class.simpleName}")

    return callback as T
}
