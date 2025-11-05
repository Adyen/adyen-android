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
import com.adyen.checkout.card.internal.ui.state.CardChangeListener
import com.adyen.checkout.card.internal.ui.state.CardComponentState
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.view.CardComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.helper.runCompileOnly
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.internal.ui.state.StateManager
import com.adyen.checkout.core.components.paymentmethod.CardPaymentMethod
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.Serializable

// TODO - Card full implementation
internal class CardComponent(
    private val analyticsManager: AnalyticsManager,
    private val stateManager: StateManager<CardViewState, CardComponentState>,
    private val cardEncryptor: BaseCardEncryptor,
    private val componentParams: ComponentParams,
) : PaymentComponent<CardPaymentComponentState>, CardChangeListener {

    private val eventChannel = bufferedChannel<PaymentComponentEvent<CardPaymentComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<CardPaymentComponentState>> =
        eventChannel.receiveAsFlow()

    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        CardNavKey to CheckoutNavEntry(CardNavKey) { backStack -> MainScreen(backStack) },
    )

    override val navigationStartingPoint: NavKey = CardNavKey

    override fun submit() {
        if (stateManager.isValid) {
            val paymentComponentState = stateManager.viewState.value.toPaymentComponentState()
            val event = PaymentComponentEvent.Submit(paymentComponentState)
            eventChannel.trySend(event)
        } else {
            stateManager.highlightAllValidationErrors()
        }
    }

    override fun setLoading(isLoading: Boolean) {
        stateManager.updateViewState {
            copy(isLoading = isLoading)
        }
    }

    override fun onCardNumberChanged(newCardNumber: String) {
        stateManager.updateViewStateAndValidate {
            copy(
                cardNumber = cardNumber.updateText(newCardNumber),
            )
        }
    }

    override fun onCardNumberFocusChanged(hasFocus: Boolean) {
        stateManager.updateViewState {
            copy(
                cardNumber = cardNumber.updateFocus(hasFocus),
            )
        }
    }

    @Composable
    private fun MainScreen(@Suppress("UNUSED_PARAMETER") backStack: NavBackStack<NavKey>) {
        val viewState by stateManager.viewState.collectAsStateWithLifecycle()
        CardComponent(
            viewState = viewState,
            changeListener = this,
            onSubmitClick = ::submit,
        )
    }

    // TODO - Card. Extract payment component state creation to a separate file.
    @Suppress("ReturnCount")
    private fun CardViewState.toPaymentComponentState(): CardPaymentComponentState {
        val unencryptedCardBuilder = UnencryptedCard.Builder()

        val publicKey = componentParams.publicKey
        if (publicKey == null) {
            return CardPaymentComponentState(
                data = PaymentComponentData(null, null, null),
                isValid = false,
            )
        }

        val encryptedCard: EncryptedCard = try {
            unencryptedCardBuilder.setNumber(cardNumber.text)
//            if (!isCvcHidden()) {
//                val cvc = outputData.securityCodeState.value
//                if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
//            }
//            val expiryDateResult = outputData.expiryDateState.value
//            if (expiryDateResult.isNotBlank()) {
//                val expiryDate = ExpiryDate.from(expiryDateResult)
//                unencryptedCardBuilder.setExpiryDate(
//                    expiryMonth = expiryDate.expiryMonth.toString(),
//                    expiryYear = expiryDate.expiryYear.toString(),
//                )
//            }

            cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
        } catch (_: EncryptionException) {
            val event = GenericEvents.error(CardPaymentMethod.PAYMENT_METHOD_TYPE, ErrorEvent.ENCRYPTION)
            analyticsManager.trackEvent(event)
//
//            exceptionChannel.trySend(e)

            return CardPaymentComponentState(
                data = PaymentComponentData(null, null, null),
                isValid = false,
            )
        }
        return mapComponentState(encryptedCard)
    }

    private fun mapComponentState(
        encryptedCard: EncryptedCard,
    ): CardPaymentComponentState {
        val cardPaymentMethod = CardPaymentMethod(
            type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
        ).apply {
            encryptedCardNumber = encryptedCard.encryptedCardNumber
//            encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth
//            encryptedExpiryYear = encryptedCard.encryptedExpiryYear

//            if (!isCvcHidden()) {
//                encryptedSecurityCode = encryptedCard.encryptedSecurityCode
//            }

//            if (isHolderNameRequired()) {
//                holderName = stateOutputData.holderNameState.value
//            }

            threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }
        }

        val paymentComponentData = PaymentComponentData<CardPaymentMethod>(
            paymentMethod = cardPaymentMethod,
            storePaymentMethod = null,
            shopperReference = null, // TODO - Card Component Params
            order = null,
            amount = componentParams.amount,
        )

        return CardPaymentComponentState(
            data = paymentComponentData,
            isValid = true,
        )
    }
}

@Serializable
private data object CardNavKey : NavKey
