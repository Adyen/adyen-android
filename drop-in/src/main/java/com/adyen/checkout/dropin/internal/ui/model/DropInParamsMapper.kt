/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/11/2022.
 */

package com.adyen.checkout.dropin.internal.ui.model

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.dropin.getDropInConfiguration
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import java.util.Locale

internal class DropInParamsMapper {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        sessionDetails: SessionDetails?,
    ): DropInParams {
        val dropInConfiguration = checkoutConfiguration.getDropInConfiguration()
        return DropInParams(
            shopperLocale = checkoutConfiguration.shopperLocale ?: deviceLocale,
            environment = checkoutConfiguration.environment,
            clientKey = checkoutConfiguration.clientKey,
            analyticsParams = AnalyticsParams(checkoutConfiguration.analyticsConfiguration),
            amount = sessionDetails?.amount ?: checkoutConfiguration.amount,
            showPreselectedStoredPaymentMethod = dropInConfiguration?.showPreselectedStoredPaymentMethod ?: true,
            skipListWhenSinglePaymentMethod = dropInConfiguration?.skipListWhenSinglePaymentMethod ?: false,
            isRemovingStoredPaymentMethodsEnabled = dropInConfiguration?.isRemovingStoredPaymentMethodsEnabled ?: false,
            additionalDataForDropInService = dropInConfiguration?.additionalDataForDropInService,
            overriddenPaymentMethodInformation = dropInConfiguration?.overriddenPaymentMethodInformation.orEmpty(),
        )
    }
}
