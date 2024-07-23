/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/7/2024.
 */

package com.adyen.checkout.mealvoucher.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.giftcard.getGiftCardConfiguration
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardComponentParams
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class MealVoucherComponentParamsMapper(
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
        // TODO Change this to getMealVoucherConfiguration()
        val giftCardConfiguration = checkoutConfiguration.getGiftCardConfiguration()
        return GiftCardComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: giftCardConfiguration?.isSubmitButtonVisible ?: true,
            // TODO Check if we need a config for pin required or not for Meal voucher
            isPinRequired = giftCardConfiguration?.isPinRequired ?: true,
            isExpiryDateRequired = true,
        )
    }
}
