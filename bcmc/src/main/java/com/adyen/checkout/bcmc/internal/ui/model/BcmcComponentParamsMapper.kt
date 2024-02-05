/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/8/2023.
 */

package com.adyen.checkout.bcmc.internal.ui.model

import com.adyen.checkout.bcmc.getBcmcConfiguration
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

internal class BcmcComponentParamsMapper(
    private val dropInOverrideParams: DropInOverrideParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        sessionParams: SessionParams?,
        paymentMethod: PaymentMethod
    ): CardComponentParams {
        return checkoutConfiguration
            .mapToParamsInternal(
                supportedCardBrands = paymentMethod.brands?.map { CardBrand(it) },
            )
            .override(dropInOverrideParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun CheckoutConfiguration.mapToParamsInternal(supportedCardBrands: List<CardBrand>?): CardComponentParams {
        val bcmcConfiguration = getBcmcConfiguration()
        return CardComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = bcmcConfiguration?.isSubmitButtonVisible ?: true,
            isHolderNameRequired = bcmcConfiguration?.isHolderNameRequired ?: false,
            shopperReference = bcmcConfiguration?.shopperReference,
            isStorePaymentFieldVisible = bcmcConfiguration?.isStorePaymentFieldVisible ?: false,
            addressParams = AddressParams.None,
            installmentParams = null,
            kcpAuthVisibility = KCPAuthVisibility.HIDE,
            socialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
            cvcVisibility = CVCVisibility.HIDE_FIRST,
            storedCVCVisibility = StoredCVCVisibility.HIDE,
            supportedCardBrands = supportedCardBrands ?: DEFAULT_SUPPORTED_CARD_BRANDS,
        )
    }

    private fun CardComponentParams.override(
        dropInOverrideParams: DropInOverrideParams?,
    ): CardComponentParams {
        if (dropInOverrideParams == null) return this
        return copy(
            amount = dropInOverrideParams.amount,
            isCreatedByDropIn = true,
        )
    }

    private fun CardComponentParams.override(
        sessionParams: SessionParams? = null
    ): CardComponentParams {
        if (sessionParams == null) return this
        return copy(
            isStorePaymentFieldVisible = sessionParams.enableStoreDetails ?: isStorePaymentFieldVisible,
            amount = sessionParams.amount ?: amount,
            shopperLocale = sessionParams.shopperLocale ?: shopperLocale,
        )
    }

    companion object {
        private val DEFAULT_SUPPORTED_CARD_BRANDS = listOf(
            CardBrand(cardType = CardType.BCMC),
            CardBrand(cardType = CardType.MAESTRO),
            CardBrand(cardType = CardType.VISA),
        )
    }
}
