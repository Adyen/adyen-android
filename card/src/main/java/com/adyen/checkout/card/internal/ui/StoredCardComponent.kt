/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.card.StoredCardNavigationKey
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateReducer
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateValidator
import com.adyen.checkout.card.internal.ui.state.StoredCardIntent
import com.adyen.checkout.card.internal.ui.state.StoredCardViewStateProducer
import com.adyen.checkout.card.internal.ui.state.toPaymentComponentState
import com.adyen.checkout.card.internal.ui.view.StoredCardComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.paymentmethod.CardPaymentMethod
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("LongParameterList")
internal class StoredCardComponent(
    private val storedPaymentMethod: StoredPaymentMethod,
    private val analyticsManager: AnalyticsManager,
    private val cardEncryptor: BaseCardEncryptor,
    private val componentParams: CardComponentParams,
    private val componentStateValidator: StoredCardComponentStateValidator,
    componentStateFactory: StoredCardComponentStateFactory,
    componentStateReducer: StoredCardComponentStateReducer,
    viewStateProducer: StoredCardViewStateProducer,
    coroutineScope: CoroutineScope,
    private val sdkDataProvider: SdkDataProvider,
) : PaymentComponent<CardPaymentComponentState> {

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
        StoredCardNavKey to CheckoutNavEntry(StoredCardNavKey, StoredCardNavigationKey) { backStack ->
            MainScreen(backStack)
        },
    )
    override val navigationStartingPoint: NavKey = StoredCardNavKey

    init {
        val cardType = CardBrand(txVariant = storedPaymentMethod.brand.orEmpty())

        val storedDetectedCardType = DetectedCardType(
            cardBrand = cardType,
            isReliable = true,
            enableLuhnCheck = true,
            cvcPolicy = when {
                componentParams.storedCVCVisibility == StoredCVCVisibility.HIDE ||
                    NO_CVC_BRANDS.contains(cardType) -> Brand.FieldPolicy.HIDDEN

                else -> Brand.FieldPolicy.REQUIRED
            },
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = true,
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )

        initializeAnalytics(coroutineScope)
        onIntent(StoredCardIntent.UpdateDetectedCardType(storedDetectedCardType))
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        analyticsManager.initialize(this, coroutineScope)
    }

    override fun submit() {
        if (componentStateValidator.isValid(componentState.value)) {
            val paymentComponentState = componentState.value.toPaymentComponentState(
                componentParams = componentParams,
                cardEncryptor = cardEncryptor,
                sdkDataProvider = sdkDataProvider,
                storedPaymentMethodId = storedPaymentMethod.id,
                onEncryptionFailed = ::onEncryptionError,
                onPublicKeyNotFound = ::onPublicKeyNotFound,
            )
            val event = PaymentComponentEvent.Submit(paymentComponentState)
            eventChannel.trySend(event)
        } else {
            onIntent(StoredCardIntent.HighlightValidationErrors)
        }
    }

    override fun setLoading(isLoading: Boolean) {
        onIntent(StoredCardIntent.UpdateLoading(isLoading))
    }

    override fun onCleared() {
        analyticsManager.clear(this)
    }

    private fun onIntent(intent: StoredCardIntent) {
        componentState.handleIntent(intent)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onEncryptionError(e: EncryptionException) {
        val event = GenericEvents.error(CardPaymentMethod.PAYMENT_METHOD_TYPE, ErrorEvent.ENCRYPTION)
        analyticsManager.trackEvent(event)
        // exceptionChannel.trySend(e)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPublicKeyNotFound(e: RuntimeException) {
        val event = GenericEvents.error(CardPaymentMethod.PAYMENT_METHOD_TYPE, ErrorEvent.API_PUBLIC_KEY)
        analyticsManager.trackEvent(event)
        // exceptionChannel.trySend(e)
    }

    @Composable
    private fun MainScreen(@Suppress("UNUSED_PARAMETER") backStack: NavBackStack<NavKey>) {
        val viewState by viewState.collectAsStateWithLifecycle()
        StoredCardComponent(
            viewState = viewState,
            onIntent = ::onIntent,
            onSubmitClick = ::submit,
        )
    }

    companion object {
        private val NO_CVC_BRANDS: Set<CardBrand> = setOf(CardBrand(txVariant = CardType.BCMC.txVariant))
    }
}
