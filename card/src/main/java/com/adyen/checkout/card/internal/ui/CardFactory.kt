/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.internal.ui.state.CardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.CardValidationMapper
import com.adyen.checkout.card.internal.ui.state.CardViewStateFactory
import com.adyen.checkout.card.internal.ui.state.CardViewStateValidator
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.PaymentMethodFactory
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.components.internal.ui.state.DefaultStateManager
import com.adyen.checkout.cse.internal.CardEncryptorFactory
import kotlinx.coroutines.CoroutineScope

internal class CardFactory : PaymentMethodFactory<CardPaymentComponentState, CardComponent> {

    override fun create(
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        // TODO - Card Component Params. Store public key in card component params and read from there.
        publicKey: String,
    ): CardComponent {
        val stateManager = DefaultStateManager(
            viewStateFactory = CardViewStateFactory(),
            componentStateFactory = CardComponentStateFactory(),
            validator = CardViewStateValidator(cardValidationMapper = CardValidationMapper())
        )

        val cardEncryptor = CardEncryptorFactory.provide()

        // TODO - Card full implementation
        return CardComponent(
            analyticsManager = analyticsManager,
            stateManager = stateManager,
            componentParams = componentParamsBundle.commonComponentParams,
            publicKey = publicKey,
            cardEncryptor = cardEncryptor,
        )
    }
}
