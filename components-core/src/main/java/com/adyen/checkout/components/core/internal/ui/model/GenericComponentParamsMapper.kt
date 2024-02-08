/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.CheckoutConfiguration
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GenericComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
    ): GenericComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        val commonComponentParams = commonComponentParamsMapperData.commonComponentParams

        return GenericComponentParams(
            commonComponentParams = commonComponentParams,
        )
    }
}
