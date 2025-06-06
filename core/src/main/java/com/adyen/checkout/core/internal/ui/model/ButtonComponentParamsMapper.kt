/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/5/2025.
 */

package com.adyen.checkout.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.CheckoutConfiguration
import com.adyen.checkout.core.internal.ButtonConfiguration
import com.adyen.checkout.core.internal.Configuration
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ButtonComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        componentConfiguration: Configuration?
    ): ButtonComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        val commonComponentParams = commonComponentParamsMapperData.commonComponentParams
        return ButtonComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: (componentConfiguration as? ButtonConfiguration)?.isSubmitButtonVisible ?: true,
        )
    }
}
