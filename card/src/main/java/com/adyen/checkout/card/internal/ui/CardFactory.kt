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
import com.adyen.checkout.card.internal.data.api.BinLookupCache
import com.adyen.checkout.card.internal.data.api.BinLookupService
import com.adyen.checkout.card.internal.data.api.DefaultDetectCardTypeRepository
import com.adyen.checkout.card.internal.data.api.LocalCardBrandDetectionService
import com.adyen.checkout.card.internal.data.api.NetworkCardBrandDetectionService
import com.adyen.checkout.card.internal.ui.model.CardComponentParamsMapper
import com.adyen.checkout.card.internal.ui.state.CardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.CardComponentStateReducer
import com.adyen.checkout.card.internal.ui.state.CardComponentStateValidator
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.CardValidationMapper
import com.adyen.checkout.card.internal.ui.state.CardViewStateProducer
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateReducer
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateValidator
import com.adyen.checkout.card.internal.ui.state.StoredCardViewStateProducer
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.StoredPaymentComponentFactory
import com.adyen.checkout.core.components.internal.data.provider.DefaultSdkDataProvider
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.components.paymentmethod.CardDetails
import com.adyen.checkout.cse.internal.CardEncryptorFactory
import com.adyen.checkout.cse.internal.GenericEncryptorFactory
import kotlinx.coroutines.CoroutineScope

internal class CardFactory :
    PaymentComponentFactory<CardPaymentComponentState, CardComponent>,
    StoredPaymentComponentFactory<CardPaymentComponentState, StoredCardComponent> {

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
            paymentMethod = paymentMethod as? CardPaymentMethod,
        )

        val cardValidationMapper = CardValidationMapper()
        val dualBrandedCardHandler = DualBrandedCardHandler()
        val componentStateFactory = CardComponentStateFactory(cardComponentParams)
        val componentStateReducer = CardComponentStateReducer(cardComponentParams)
        val componentStateValidator = CardComponentStateValidator(cardValidationMapper)
        val viewStateProducer = CardViewStateProducer(dualBrandedCardHandler)

        val cardEncryptor = CardEncryptorFactory.provide()
        val genericEncryptor = GenericEncryptorFactory.provide()
        val httpClient = HttpClientFactory.getHttpClient(cardComponentParams.environment)
        val binLookupService = BinLookupService(httpClient, cardComponentParams.clientKey)
        val binLookupCache = BinLookupCache()
        val localCardBrandDetectionService = LocalCardBrandDetectionService(cardComponentParams.supportedCardBrands)
        val networkCardBrandDetectionService = NetworkCardBrandDetectionService(
            cardEncryptor,
            binLookupService,
            cardComponentParams.publicKey,
            cardComponentParams.supportedCardBrands,
            // TODO ensure this is set to BCMC in the BCMC factory supported
            paymentMethodType = CardDetails.PAYMENT_METHOD_TYPE,
        )
        val detectCardTypeRepository = DefaultDetectCardTypeRepository(
            binLookupCache,
            localCardBrandDetectionService,
            networkCardBrandDetectionService,
        )

        return CardComponent(
            analyticsManager = analyticsManager,
            cardEncryptor = cardEncryptor,
            genericEncryptor = genericEncryptor,
            componentParams = cardComponentParams,
            detectCardTypeRepository = detectCardTypeRepository,
            componentStateValidator = componentStateValidator,
            componentStateFactory = componentStateFactory,
            componentStateReducer = componentStateReducer,
            viewStateProducer = viewStateProducer,
            coroutineScope = coroutineScope,
            sdkDataProvider = DefaultSdkDataProvider(analyticsManager),
        ).apply {
            // TODO - Find an alternative way, so we don't need the callbacks
            setOnBinValueCallback(checkoutCallbacks.getAdditionalCallback(OnBinValueCallback::class))
            setOnBinLookupCallback(checkoutCallbacks.getAdditionalCallback(OnBinLookupCallback::class))
        }
    }

    override fun create(
        storedPaymentMethod: StoredPaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        @Suppress("UNUSED_PARAMETER") checkoutCallbacks: CheckoutCallbacks,
    ): StoredCardComponent {
        val cardComponentParams = CardComponentParamsMapper().mapToParams(
            componentParamsBundle = componentParamsBundle,
            cardConfiguration = checkoutConfiguration.getCardConfiguration(),
            paymentMethod = null,
        )

        val cardValidationMapper = CardValidationMapper()
        val componentStateFactory = StoredCardComponentStateFactory(cardComponentParams)
        val componentStateReducer = StoredCardComponentStateReducer()
        val componentStateValidator = StoredCardComponentStateValidator(cardValidationMapper)
        val viewStateProducer = StoredCardViewStateProducer()

        val cardEncryptor = CardEncryptorFactory.provide()

        return StoredCardComponent(
            // TODO - Remove casting when paymentMethod object is typed
            storedPaymentMethod = storedPaymentMethod as? StoredCardPaymentMethod ?: error("Incorrect paymentMethod"),
            analyticsManager = analyticsManager,
            cardEncryptor = cardEncryptor,
            componentParams = cardComponentParams,
            componentStateValidator = componentStateValidator,
            componentStateFactory = componentStateFactory,
            componentStateReducer = componentStateReducer,
            viewStateProducer = viewStateProducer,
            coroutineScope = coroutineScope,
            sdkDataProvider = DefaultSdkDataProvider(analyticsManager),
        )
    }
}
