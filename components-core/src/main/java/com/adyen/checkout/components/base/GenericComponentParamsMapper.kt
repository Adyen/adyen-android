/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.components.base

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GenericComponentParamsMapper(
    private val parentConfiguration: Configuration?
) {

    fun mapToParams(
        configuration: Configuration,
    ): GenericComponentParams {
        with(parentConfiguration ?: configuration) {
            return GenericComponentParams(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )
        }
    }
}
