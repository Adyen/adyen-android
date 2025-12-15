/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.OnBinLookupCallback
import com.adyen.checkout.card.OnBinValueCallback
import com.adyen.checkout.card.getCardConfiguration
import com.adyen.checkout.card.internal.data.api.BinLookupService
import com.adyen.checkout.card.internal.data.api.DefaultDetectCardTypeRepository
import com.adyen.checkout.card.internal.ui.model.CardComponentParamsMapper
import com.adyen.checkout.card.internal.ui.state.CardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.CardValidationMapper
import com.adyen.checkout.card.internal.ui.state.CardViewStateFactory
import com.adyen.checkout.card.internal.ui.state.CardViewStateValidator
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.internal.PaymentMethodFactory
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.components.internal.ui.state.DefaultStateManager
import com.adyen.checkout.cse.internal.CardEncryptorFactory
import kotlinx.coroutines.CoroutineScope

internal class CardFactory : PaymentMethodFactory<CardPaymentComponentState, CardComponent> {

    override fun create(
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
    ): CardComponent {
        val cardComponentParams = CardComponentParamsMapper().mapToParams(
            componentParamsBundle = componentParamsBundle,
            cardConfiguration = checkoutConfiguration.getCardConfiguration(),
            paymentMethod = paymentMethod,
        )

        val stateManager = DefaultStateManager(
            viewStateFactory = CardViewStateFactory(
                componentParams = cardComponentParams,
            ),
            componentStateFactory = CardComponentStateFactory(cardComponentParams),
            validator = CardViewStateValidator(
                cardValidationMapper = CardValidationMapper(),
                dualBrandedCardHandler = DualBrandedCardHandler(),
            ),
        )

        val cardEncryptor = CardEncryptorFactory.provide()
        val httpClient = HttpClientFactory.getHttpClient(componentParamsBundle.commonComponentParams.environment)
        val binLookupService = BinLookupService(httpClient)
        val detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncryptor, binLookupService)

        return CardComponent(
            analyticsManager = analyticsManager,
            stateManager = stateManager,
            componentParams = cardComponentParams,
            cardEncryptor = cardEncryptor,
            detectCardTypeRepository = detectCardTypeRepository,
        ).apply {
            initialize(coroutineScope)
            setOnBinValueCallback(checkoutCallbacks.getCallback(OnBinValueCallback::class))
            setOnBinLookupCallback(checkoutCallbacks.getCallback(OnBinLookupCallback::class))
        }
    }
}
