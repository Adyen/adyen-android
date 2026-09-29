/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateFactory
import com.adyen.checkout.address.internal.ui.state.AddressComponentStatePostProcessor
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateReducer
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateValidator
import com.adyen.checkout.address.internal.ui.state.AddressViewStateProducer
import kotlinx.coroutines.CoroutineScope

/**
 * Creates the [AddressComponent] for the component that hosts the address form.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AddressComponentFactory {

    fun create(
        componentParams: AddressComponentParams,
        coroutineScope: CoroutineScope,
    ): AddressComponent {
        val componentStateFactory = AddressComponentStateFactory(componentParams)
        return AddressComponent(
            componentStateValidator = AddressComponentStateValidator(),
            componentStateFactory = componentStateFactory,
            componentStateReducer = AddressComponentStateReducer(componentStateFactory),
            componentStatePostProcessor = AddressComponentStatePostProcessor(),
            viewStateProducer = AddressViewStateProducer(),
            coroutineScope = coroutineScope,
        )
    }
}
