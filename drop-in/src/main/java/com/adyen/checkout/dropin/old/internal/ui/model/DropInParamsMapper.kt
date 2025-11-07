/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.ui.model

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.dropin.old.DropInConfiguration
import com.adyen.checkout.dropin.old.getDropInConfiguration
import java.util.Locale

internal class DropInParamsMapper {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        sessionParams: SessionParams?,
    ): com.adyen.checkout.dropin.old.internal.ui.model.DropInParams {
        val dropInConfiguration = checkoutConfiguration.getDropInConfiguration()
        return com.adyen.checkout.dropin.old.internal.ui.model.DropInParams(
            shopperLocale = getShopperLocale(checkoutConfiguration, sessionParams) ?: deviceLocale,
            environment = sessionParams?.environment ?: checkoutConfiguration.environment,
            clientKey = sessionParams?.clientKey ?: checkoutConfiguration.clientKey,
            analyticsParams = AnalyticsParams(
                analyticsConfiguration = checkoutConfiguration.analyticsConfiguration,
                clientKey = checkoutConfiguration.clientKey,
            ),
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
