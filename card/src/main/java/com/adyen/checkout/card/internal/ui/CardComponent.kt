/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.card.CardMainNavigationKey
import com.adyen.checkout.card.OnBinLookupCallback
import com.adyen.checkout.card.OnBinValueCallback
import com.adyen.checkout.card.internal.data.api.DetectCardTypeRepository
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.helper.toBinLookupData
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.state.CardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.CardComponentStateReducer
import com.adyen.checkout.card.internal.ui.state.CardComponentStateValidator
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.CardViewStateProducer
import com.adyen.checkout.card.internal.ui.state.binValue
import com.adyen.checkout.card.internal.ui.state.toPaymentComponentState
import com.adyen.checkout.card.internal.ui.view.CardComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.paymentmethod.CardDetails
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("TooManyFunctions", "LongParameterList")
internal class CardComponent(
    private val analyticsManager: AnalyticsManager,
    private val cardEncryptor: BaseCardEncryptor,
    private val genericEncryptor: BaseGenericEncryptor,
    private val componentParams: CardComponentParams,
    private val detectCardTypeRepository: DetectCardTypeRepository,
    private val componentStateValidator: CardComponentStateValidator,
    componentStateFactory: CardComponentStateFactory,
    componentStateReducer: CardComponentStateReducer,
    viewStateProducer: CardViewStateProducer,
    private val coroutineScope: CoroutineScope,
    private val sdkDataProvider: SdkDataProvider,
) : PaymentComponent<CardPaymentComponentState> {

    private var onBinValueCallback: OnBinValueCallback? = null
    private var onBinLookupCallback: OnBinLookupCallback? = null

    private val eventChannel = bufferedChannel<PaymentComponentEvent<CardPaymentComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<CardPaymentComponentState>> =
        eventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = coroutineScope,
    )

    private val viewState = componentState.viewState(viewStateProducer, coroutineScope)

    // TODO - Remove navigation
    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        CardNavKey to CheckoutNavEntry(CardNavKey, CardMainNavigationKey) { },
    )

    override val navigationStartingPoint: NavKey = CardNavKey

    init {
        initializeAnalytics()
        subscribeToDetectedCardTypesChanges()
        subscribeToBinChanges()
    }

    private fun initializeAnalytics() {
        analyticsManager.initialize(this, coroutineScope)
    }

    fun setOnBinValueCallback(onBinValueCallback: OnBinValueCallback?) {
        this.onBinValueCallback = onBinValueCallback
    }

    fun setOnBinLookupCallback(onBinLookupCallback: OnBinLookupCallback?) {
        this.onBinLookupCallback = onBinLookupCallback
    }

    override fun submit() {
        if (componentStateValidator.isValid(componentState.value)) {
            val paymentComponentState = componentState.value.toPaymentComponentState(
                componentParams = componentParams,
                cardEncryptor = cardEncryptor,
                genericEncryptor = genericEncryptor,
                sdkDataProvider = sdkDataProvider,
                onEncryptionFailed = ::onEncryptionError,
                onPublicKeyNotFound = ::onPublicKeyNotFound,
            )
            val event = PaymentComponentEvent.Submit(paymentComponentState)
            eventChannel.trySend(event)
        } else {
            onIntent(CardIntent.HighlightValidationErrors)
        }
    }

    override fun setLoading(isLoading: Boolean) {
        onIntent(CardIntent.UpdateLoading(isLoading))
    }

    override fun onCleared() {
        analyticsManager.clear(this)
    }

    private fun onIntent(intent: CardIntent) {
        componentState.handleIntent(intent)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewState by viewState.collectAsStateWithLifecycle()
        CardComponent(
            viewState = viewState,
            onIntent = ::handleIntent,
            onSubmitClick = ::submit,
        )
    }

    private fun handleIntent(intent: CardIntent) {
        when (intent) {
            is CardIntent.UpdateCardNumber -> onCardNumberChanged(intent.number)
            else -> onIntent(intent)
        }
    }

    private fun onCardNumberChanged(newCardNumber: String) {
        detectCardTypes(newCardNumber)
        onIntent(CardIntent.UpdateCardNumber(newCardNumber))
    }

    private fun detectCardTypes(cardNumber: String) {
        val publicKey = componentParams.publicKey
        if (publicKey == null) {
            onPublicKeyNotFound(GenericError("Public key is missing."))
            return
        }

        detectCardTypeRepository.detectCardTypes(
            cardNumber = cardNumber,
            publicKey = publicKey,
            supportedCardBrands = componentParams.supportedCardBrands,
            clientKey = componentParams.clientKey,
            // TODO ensure this is set dynamically when BCMC is supported
            paymentMethodType = CardDetails.PAYMENT_METHOD_TYPE,
        ).onEach { result ->
            onIntent(CardIntent.UpdateDetectedCardTypes(result))
        }.launchIn(coroutineScope)
    }

    private fun subscribeToBinChanges() {
        componentState
            .map { it.binValue }
            .distinctUntilChanged()
            .drop(1)
            .onEach { newBinValue -> onBinValueCallback?.onBinValue(newBinValue) }
            .launchIn(coroutineScope)
    }

    private fun subscribeToDetectedCardTypesChanges() {
        componentState
            .map { it.detectedCardTypes }
            .distinctUntilChanged()
            .drop(1)
            .onEach(::onDetectedCardTypesChanged)
            .launchIn(coroutineScope)
    }

    private fun onDetectedCardTypesChanged(detectedCardTypes: List<DetectedCardType>) {
        val isReliable = detectedCardTypes.any { it.isReliable }
        if (isReliable) {
            onBinLookupCallback?.onBinLookup(
                data = detectedCardTypes.map(DetectedCardType::toBinLookupData),
            )
        }
    }

    private fun onEncryptionError(e: EncryptionException) {
        val event = GenericEvents.error(CardDetails.PAYMENT_METHOD_TYPE, ErrorEvent.ENCRYPTION)
        analyticsManager.trackEvent(event)

        // TODO - Error propagation. Change after EncryptionException extends from CheckoutError
        emitError(GenericError("Encryption error", e))
    }

    private fun emitError(error: InternalCheckoutError) {
        eventChannel.trySend(
            PaymentComponentEvent.Error(error),
        )
    }

    // TODO - Error propagation. Change after implementation of specific error for this case
    private fun onPublicKeyNotFound(e: InternalCheckoutError) {
        val event = GenericEvents.error(CardDetails.PAYMENT_METHOD_TYPE, ErrorEvent.API_PUBLIC_KEY)
        analyticsManager.trackEvent(event)
        emitError(e)
    }
}
