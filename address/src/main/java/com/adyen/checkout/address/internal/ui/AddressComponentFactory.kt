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
import com.adyen.checkout.core.common.internal.CheckoutParams
import kotlinx.coroutines.CoroutineScope

/**
 * Creates the [AddressComponent] for the component that hosts the address form.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AddressComponentFactory {

    /**
     * @param params The params of the host. The address component takes what it needs from them.
     * @param supportedCountryCodes The ISO codes of the countries the shopper can pick. When empty, every country can
     * be picked.
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
            componentStateReducer = AddressComponentStateReducer(componentStateFactory),
            componentStatePostProcessor = AddressComponentStatePostProcessor(),
            viewStateProducer = AddressViewStateProducer(),
            coroutineScope = coroutineScope,
        )
    }
}
