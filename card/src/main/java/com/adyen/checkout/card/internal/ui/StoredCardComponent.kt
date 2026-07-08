/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.card.internal.helper.CardConfigDataGenerator
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentState
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateReducer
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateValidator
import com.adyen.checkout.card.internal.ui.state.StoredCardIntent
import com.adyen.checkout.card.internal.ui.state.StoredCardViewStateProducer
import com.adyen.checkout.card.internal.ui.view.StoredCardContent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.helper.runCompileOnly
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.getPaymentDataValue
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.paymentmethod.CardDetails
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("TooManyFunctions")
internal class StoredCardComponent
@Suppress("LongParameterList")
constructor(
    private val storedPaymentMethod: StoredCardPaymentMethod,
    private val analyticsManager: AnalyticsManager,
    private val cardEncryptor: BaseCardEncryptor,
    private val componentStateValidator: StoredCardComponentStateValidator,
    componentStateFactory: StoredCardComponentStateFactory,
    componentStateReducer: StoredCardComponentStateReducer,
    viewStateProducer: StoredCardViewStateProducer,
    coroutineScope: CoroutineScope,
    private val sdkDataProvider: SdkDataProvider,
    private val publicKey: String?,
    private val paymentMethodType: String,
    private val componentParams: CardComponentParams,
    private val cardConfigDataGenerator: CardConfigDataGenerator,
) : PaymentComponent {

    private val eventChannel = bufferedChannel<PaymentComponentEvent>()
    override val eventFlow: Flow<PaymentComponentEvent> = eventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = coroutineScope,
    )

    private val viewState = componentState.viewState(viewStateProducer, coroutineScope)

    init {
        trackRenderEvent()
    }

    private fun trackRenderEvent() {
        val event = GenericEvents.rendered(
            component = paymentMethodType,
            isStoredPaymentMethod = true,
            configData = cardConfigDataGenerator.generate(params = componentParams, isStored = true),
        )
        analyticsManager.trackEvent(event)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        StoredCardContent(
            viewStateFlow = viewState,
            onIntent = ::onIntent,
            onSubmitClick = ::submit,
            modifier = modifier,
        )
    }

    override fun submit() {
        if (componentStateValidator.isValid(componentState.value)) {
            val encryptedCard = componentState.value.encryptCard() ?: return
            val paymentComponentState = createPaymentComponentState(
                encryptedCard = encryptedCard,
                sdkDataProvider = sdkDataProvider,
                storedPaymentMethodId = storedPaymentMethod.id,
                paymentMethodType = storedPaymentMethod.type,
            )
            val event = PaymentComponentEvent.Submit(paymentComponentState)
            eventChannel.trySend(event)
        } else {
            onIntent(StoredCardIntent.HighlightValidationErrors)
        }
    }

    private fun StoredCardComponentState.encryptCard(): EncryptedCard? {
        publicKey ?: run {
            onPublicKeyNotFound()
            return null
        }
        return try {
            val unencryptedCardBuilder = UnencryptedCard.Builder()
            securityCode.getPaymentDataValue()?.let {
                unencryptedCardBuilder.setCvc(it)
            }
            cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
        } catch (e: EncryptionException) {
            onEncryptionError(e)
            null
        }
    }

    private fun createPaymentComponentState(
        encryptedCard: EncryptedCard,
        sdkDataProvider: SdkDataProvider,
        storedPaymentMethodId: String,
        paymentMethodType: String,
    ): CardPaymentComponentState {
        val cardDetails = CardDetails(
            type = paymentMethodType,
            sdkData = sdkDataProvider.createEncodedSdkData(
                threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }.getOrNull(),
            ),
            storedPaymentMethodId = storedPaymentMethodId,
            encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = cardDetails,
            storePaymentMethod = null,
            order = null,
        )

        return CardPaymentComponentState(
            data = paymentComponentData,
            isValid = true,
        )
    }

    override fun requiresUserInteraction(): Boolean =
        componentState.value.securityCode.requirementPolicy == RequirementPolicy.Required

    override fun setLoading(isLoading: Boolean) {
        onIntent(StoredCardIntent.UpdateLoading(isLoading))
    }

    private fun onIntent(intent: StoredCardIntent) {
        componentState.handleIntent(intent)
    }

    private fun onEncryptionError(e: EncryptionException) {
        val event = GenericEvents.error(storedPaymentMethod.type, ErrorEvent.ENCRYPTION)
        analyticsManager.trackEvent(event)
        emitError(GenericError("Encryption error", e))
    }

    private fun onPublicKeyNotFound() {
        val event = GenericEvents.error(storedPaymentMethod.type, ErrorEvent.API_PUBLIC_KEY)
        analyticsManager.trackEvent(event)
        emitError(GenericError("Public key is missing."))
    }

    private fun emitError(error: InternalCheckoutError) {
        eventChannel.trySend(
            PaymentComponentEvent.Error(error),
        )
    }

    override fun onCleared() = Unit
}
