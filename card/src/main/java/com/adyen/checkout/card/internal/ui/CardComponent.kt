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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
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
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.error.internal.CheckoutError
import com.adyen.checkout.core.error.internal.ComponentError
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.internal.BaseCardEncryptor
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

    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        CardNavKey to CheckoutNavEntry(CardNavKey, CardMainNavigationKey) { backStack -> MainScreen(backStack) },
    )

    override val navigationStartingPoint: NavKey = CardNavKey

    init {
        initializeAnalytics()
        subscribeToDetectedCardTypes()
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
    private fun MainScreen(@Suppress("UNUSED_PARAMETER") backStack: NavBackStack<NavKey>) {
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
        detectCardTypeRepository.detectCardType(
            cardNumber = newCardNumber,
            publicKey = componentParams.publicKey,
            supportedCardBrands = componentParams.supportedCardBrands,
            clientKey = componentParams.clientKey,
            coroutineScope = coroutineScope,
            type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
        )
        onIntent(CardIntent.UpdateCardNumber(newCardNumber))
    }

    private fun subscribeToDetectedCardTypes() {
        detectCardTypeRepository.detectedCardTypesFlow
            .onEach { detectedCardTypes ->
                onDetectedCardTypes(detectedCardTypes)
            }
            .map { detectedCardTypes ->
                detectedCardTypes.filter { it.isReliable && it.isSupported }.map { it.cardBrand }
            }
            .distinctUntilChanged()
            .onEach {
                // TODO - Card. Dual brands reset brand
                // inputData.selectedCardBrand = null
            }
            .launchIn(coroutineScope)
    }

    private fun subscribeToBinChanges() {
        componentState
            .map { it.binValue }
            .distinctUntilChanged()
            .drop(1)
            .onEach { newBinValue -> onBinValueCallback?.onBinValue(newBinValue) }
            .launchIn(coroutineScope)
    }

    private fun onDetectedCardTypes(detectedCardTypes: List<DetectedCardType>) {
        adyenLog(AdyenLogLevel.DEBUG) {
            "New detected card types emitted - detectedCardTypes: ${detectedCardTypes.map { it.cardBrand }} " +
                "- isReliable: ${detectedCardTypes.firstOrNull()?.isReliable}"
        }
        val isReliable = detectedCardTypes.any { it.isReliable }
        if (componentState.value.detectedCardTypes != detectedCardTypes && isReliable) {
            onBinLookupCallback?.onBinLookup(
                data = detectedCardTypes.map(DetectedCardType::toBinLookupData),
            )
        }
        onIntent(CardIntent.UpdateDetectedCardTypes(detectedCardTypes))
    }

    private fun onEncryptionError(e: EncryptionException) {
        val event = GenericEvents.error(CardPaymentMethod.PAYMENT_METHOD_TYPE, ErrorEvent.ENCRYPTION)
        analyticsManager.trackEvent(event)

        // TODO - Error propagation. Change after EncryptionException extends from CheckoutError
        emitError(ComponentError("Encryption error", e))
    }

    private fun emitError(error: CheckoutError) {
        eventChannel.trySend(
            PaymentComponentEvent.Error(error),
        )
    }

    // TODO - Error propagation. Change after implementation of specific error for this case
    private fun onPublicKeyNotFound(e: ComponentError) {
        val event = GenericEvents.error(CardPaymentMethod.PAYMENT_METHOD_TYPE, ErrorEvent.API_PUBLIC_KEY)
        analyticsManager.trackEvent(event)
        emitError(e)
    }
}
