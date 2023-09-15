/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/8/2023.
 */

package com.adyen.checkout.bcmc.internal.ui.model

import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

internal class BcmcComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        bcmcConfiguration: BcmcConfiguration,
        sessionParams: SessionParams?,
    ): CardComponentParams {
        return bcmcConfiguration
            .mapToParamsInternal()
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun BcmcConfiguration.mapToParamsInternal(): CardComponentParams {
        return CardComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = isSubmitButtonVisible ?: true,
            isHolderNameRequired = isHolderNameRequired ?: false,
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = isStorePaymentFieldVisible ?: false,
            addressParams = AddressParams.None,
            installmentParams = null,
            kcpAuthVisibility = KCPAuthVisibility.HIDE,
            socialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
            cvcVisibility = CVCVisibility.HIDE_FIRST,
            storedCVCVisibility = StoredCVCVisibility.HIDE,
            supportedCardBrands = listOf(
                CardBrand(cardType = CardType.BCMC),
                CardBrand(cardType = CardType.MAESTRO),
                CardBrand(cardType = CardType.VISA)
            )
        )
    }

    private fun CardComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): CardComponentParams {
        if (overrideComponentParams == null) return this
        return copy(
            shopperLocale = overrideComponentParams.shopperLocale,
            environment = overrideComponentParams.environment,
            clientKey = overrideComponentParams.clientKey,
            analyticsParams = overrideComponentParams.analyticsParams,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = overrideComponentParams.amount,
        )
    }

    private fun CardComponentParams.override(
        sessionParams: SessionParams? = null
    ): CardComponentParams {
        if (sessionParams == null) return this
        return copy(
            isStorePaymentFieldVisible = sessionParams.enableStoreDetails ?: isStorePaymentFieldVisible,
            amount = sessionParams.amount ?: amount,
        )
    }
}
