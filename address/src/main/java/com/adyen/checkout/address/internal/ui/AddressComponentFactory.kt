/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
 */

package com.adyen.checkout.address.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateFactory
import com.adyen.checkout.address.internal.ui.state.AddressComponentStatePostProcessor
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateReducer
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateValidator
import com.adyen.checkout.address.internal.ui.state.AddressViewStateProducer
import com.adyen.checkout.core.common.internal.CheckoutParams
import kotlinx.coroutines.CoroutineScope

/**
 * Creates the [AddressComponent] for the component that hosts the address form.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AddressComponentFactory {

    /**
     * @param params The params of the host.
     * @param supportedCountryCodes The ISO 3166-1 alpha-2 codes of the countries the shopper can pick. When empty,
     * every country can be picked.
     * @param coroutineScope The scope of the host. The component's view state lives in it.
     */
    fun create(
        params: CheckoutParams,
        supportedCountryCodes: Set<String>,
        coroutineScope: CoroutineScope,
    ): AddressComponent {
        val componentParams = AddressComponentParams(
            shopperLocale = params.shopperLocale,
            supportedCountryCodes = supportedCountryCodes,
        )
        val componentStateFactory = AddressComponentStateFactory(componentParams)
        return AddressComponent(
            componentStateValidator = AddressComponentStateValidator(),
            componentStateFactory = componentStateFactory,
            componentStateReducer = AddressComponentStateReducer(componentParams, componentStateFactory),
            componentStatePostProcessor = AddressComponentStatePostProcessor(),
            viewStateProducer = AddressViewStateProducer(params.shopperLocale),
            coroutineScope = coroutineScope,
        )
    }
}
