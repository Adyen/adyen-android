/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/11/2023.
 */

package com.adyen.checkout.giftcard.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.giftcard.getGiftCardConfiguration
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GiftCardComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
    ): GiftCardComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        val commonComponentParams = commonComponentParamsMapperData.commonComponentParams
        val giftCardConfiguration = checkoutConfiguration.getGiftCardConfiguration()
        return GiftCardComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: giftCardConfiguration?.isSubmitButtonVisible ?: true,
            isPinRequired = giftCardConfiguration?.isPinRequired ?: true,
        )
    }
}
