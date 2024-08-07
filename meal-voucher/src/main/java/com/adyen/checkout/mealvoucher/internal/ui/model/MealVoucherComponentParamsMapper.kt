/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/7/2024.
 */

package com.adyen.checkout.mealvoucher.internal.ui.model

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardComponentParams
import com.adyen.checkout.mealvoucher.getMealVoucherConfiguration
import java.util.Locale

internal class MealVoucherComponentParamsMapper(
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
        val mealVoucherConfiguration = checkoutConfiguration.getMealVoucherConfiguration()
        return GiftCardComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: mealVoucherConfiguration?.isSubmitButtonVisible ?: true,
            isPinRequired = mealVoucherConfiguration?.isSecurityCodeRequired ?: true,
            isExpiryDateRequired = true,
        )
    }
}
