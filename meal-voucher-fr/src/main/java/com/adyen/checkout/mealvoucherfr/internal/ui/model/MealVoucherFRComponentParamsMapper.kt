/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/9/2024.
 */

package com.adyen.checkout.mealvoucherfr.internal.ui.model

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardComponentParams
import com.adyen.checkout.mealvoucherfr.getMealVoucherFRConfiguration
import java.util.Locale

internal class MealVoucherFRComponentParamsMapper(
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
        val mealVoucherFRConfiguration = checkoutConfiguration.getMealVoucherFRConfiguration()
        return GiftCardComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: mealVoucherFRConfiguration?.isSubmitButtonVisible
                ?: checkoutConfiguration.isSubmitButtonVisible
                ?: true,
            isPinRequired = mealVoucherFRConfiguration?.isSecurityCodeRequired ?: true,
            isExpiryDateRequired = true,
        )
    }
}
