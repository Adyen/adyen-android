/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/8/2023.
 */

package com.adyen.checkout.bcmc.internal.ui.model

import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.bcmc.getBcmcConfiguration
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.old.CardBrand
import com.adyen.checkout.core.old.CardType
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import java.util.Locale

internal class BcmcComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        paymentMethod: PaymentMethod,
    ): CardComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        val bcmcConfiguration = checkoutConfiguration.getBcmcConfiguration()
        return mapToParams(
            commonComponentParamsMapperData.commonComponentParams,
            commonComponentParamsMapperData.sessionParams,
            dropInOverrideParams,
            bcmcConfiguration,
            paymentMethod,
        )
    }

    private fun mapToParams(
        commonComponentParams: CommonComponentParams,
        sessionParams: SessionParams?,
        dropInOverrideParams: DropInOverrideParams?,
        bcmcConfiguration: BcmcConfiguration?,
        paymentMethod: PaymentMethod,
    ): CardComponentParams {
        return CardComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: bcmcConfiguration?.isSubmitButtonVisible ?: true,
            isHolderNameRequired = bcmcConfiguration?.isHolderNameRequired ?: false,
            shopperReference = bcmcConfiguration?.shopperReference,
            isStorePaymentFieldVisible = getStorePaymentFieldVisible(sessionParams, bcmcConfiguration),
            addressParams = AddressParams.None,
            installmentParams = null,
            kcpAuthVisibility = KCPAuthVisibility.HIDE,
            socialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
            cvcVisibility = CVCVisibility.HIDE_FIRST,
            storedCVCVisibility = StoredCVCVisibility.HIDE,
            supportedCardBrands = getSupportedCardBrands(paymentMethod),
        )
    }

    private fun getStorePaymentFieldVisible(
        sessionParams: SessionParams?,
        bcmcConfiguration: BcmcConfiguration?,
    ): Boolean {
        return sessionParams?.enableStoreDetails ?: bcmcConfiguration?.isStorePaymentFieldVisible ?: false
    }

    private fun getSupportedCardBrands(paymentMethod: PaymentMethod): List<CardBrand> {
        return paymentMethod.brands?.map { CardBrand(it) } ?: DEFAULT_SUPPORTED_CARD_BRANDS
    }

    companion object {
        private val DEFAULT_SUPPORTED_CARD_BRANDS = listOf(
            CardBrand(cardType = CardType.BCMC),
            CardBrand(cardType = CardType.MAESTRO),
            CardBrand(cardType = CardType.VISA),
        )
    }
}
