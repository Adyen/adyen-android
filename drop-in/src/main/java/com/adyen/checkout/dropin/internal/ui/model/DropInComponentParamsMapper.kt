/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/11/2022.
 */

package com.adyen.checkout.dropin.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.dropin.getDropInConfiguration

internal class DropInComponentParamsMapper {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        overrideAmount: Amount?,
    ): DropInComponentParams {
        val dropInConfiguration = checkoutConfiguration.getDropInConfiguration()
        return DropInComponentParams(
            shopperLocale = checkoutConfiguration.shopperLocale,
            environment = checkoutConfiguration.environment,
            clientKey = checkoutConfiguration.clientKey,
            analyticsParams = AnalyticsParams(checkoutConfiguration.analyticsConfiguration),
            isCreatedByDropIn = true,
            amount = overrideAmount,
            showPreselectedStoredPaymentMethod = dropInConfiguration?.showPreselectedStoredPaymentMethod ?: true,
            skipListWhenSinglePaymentMethod = dropInConfiguration?.skipListWhenSinglePaymentMethod ?: false,
            isRemovingStoredPaymentMethodsEnabled = dropInConfiguration?.isRemovingStoredPaymentMethodsEnabled ?: false,
            additionalDataForDropInService = dropInConfiguration?.additionalDataForDropInService,
        )
    }
}
