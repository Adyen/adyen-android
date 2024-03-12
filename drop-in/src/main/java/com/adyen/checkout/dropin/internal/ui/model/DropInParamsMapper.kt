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
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.getDropInConfiguration
import java.util.Locale

internal class DropInParamsMapper {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        sessionParams: SessionParams?,
    ): DropInParams {
        val dropInConfiguration = checkoutConfiguration.getDropInConfiguration()
        return DropInParams(
            shopperLocale = getShopperLocale(checkoutConfiguration, sessionParams) ?: deviceLocale,
            environment = sessionParams?.environment ?: checkoutConfiguration.environment,
            clientKey = sessionParams?.clientKey ?: checkoutConfiguration.clientKey,
            analyticsParams = AnalyticsParams(checkoutConfiguration.analyticsConfiguration),
            amount = sessionParams?.amount ?: checkoutConfiguration.amount,
            showPreselectedStoredPaymentMethod = dropInConfiguration?.showPreselectedStoredPaymentMethod ?: true,
            skipListWhenSinglePaymentMethod = dropInConfiguration?.skipListWhenSinglePaymentMethod ?: false,
            isRemovingStoredPaymentMethodsEnabled = getIsRemovingStoredPaymentMethodsEnabled(
                dropInConfiguration,
                sessionParams,
            ) ?: false,
            additionalDataForDropInService = dropInConfiguration?.additionalDataForDropInService,
            overriddenPaymentMethodInformation = dropInConfiguration?.overriddenPaymentMethodInformation.orEmpty(),
        )
    }

    fun getShopperLocale(checkoutConfiguration: CheckoutConfiguration, sessionParams: SessionParams?): Locale? {
        return checkoutConfiguration.shopperLocale ?: sessionParams?.shopperLocale
    }

    private fun getIsRemovingStoredPaymentMethodsEnabled(
        dropInConfiguration: DropInConfiguration?,
        sessionParams: SessionParams?
    ) = sessionParams?.showRemovePaymentMethodButton ?: dropInConfiguration?.isRemovingStoredPaymentMethodsEnabled
}
