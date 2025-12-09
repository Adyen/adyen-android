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
import com.adyen.checkout.card.internal.ui.state.StoredCardChangeListener
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentState
import com.adyen.checkout.card.internal.ui.state.StoredCardViewState
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
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.internal.ui.state.StateManager
import com.adyen.checkout.core.components.paymentmethod.CardPaymentMethod
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class StoredCardComponent(
    private val storedPaymentMethod: StoredPaymentMethod,
    private val analyticsManager: AnalyticsManager,
    private val stateManager: StateManager<StoredCardViewState, StoredCardComponentState>,
    private val cardEncryptor: BaseCardEncryptor,
    private val componentParams: CardComponentParams,
) : PaymentComponent<CardPaymentComponentState>, StoredCardChangeListener {

    private val eventChannel = bufferedChannel<PaymentComponentEvent<CardPaymentComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<CardPaymentComponentState>> =
        eventChannel.receiveAsFlow()

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

        stateManager.updateComponentState {
            copy(detectedCardType = storedDetectedCardType)
        }
    }

    override fun submit() {
        if (stateManager.isValid) {
            val paymentComponentState = stateManager.viewState.value.toPaymentComponentState(
                componentParams = componentParams,
                cardEncryptor = cardEncryptor,
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                storedPaymentMethodId = storedPaymentMethod.id,
                onEncryptionFailed = ::onEncryptionError,
                onPublicKeyNotFound = ::onPublicKeyNotFound,
            )
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

    @Suppress("UNUSED_PARAMETER")
    private fun onEncryptionError(e: EncryptionException) {
        val event = GenericEvents.error(CardPaymentMethod.PAYMENT_METHOD_TYPE, ErrorEvent.ENCRYPTION)
        analyticsManager.trackEvent(event)
        // exceptionChannel.trySend(e)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPublicKeyNotFound(e: RuntimeException) {
        // TODO - Analytics.
        // exceptionChannel.trySend(e)
    }

    @Composable
    private fun MainScreen(@Suppress("UNUSED_PARAMETER") backStack: NavBackStack<NavKey>) {
        val viewState by stateManager.viewState.collectAsStateWithLifecycle()
        StoredCardComponent(
            viewState = viewState,
            changeListener = this,
            onSubmitClick = ::submit,
        )
    }

    override fun onSecurityCodeChanged(newSecurityCode: String) {
        stateManager.updateViewStateAndValidate {
            copy(
                securityCode = securityCode.updateText(newSecurityCode),
            )
        }
    }

    override fun onSecurityCodeFocusChanged(hasFocus: Boolean) {
        stateManager.updateViewState {
            copy(
                securityCode = securityCode.updateFocus(hasFocus),
            )
        }
    }

    companion object {
        private val NO_CVC_BRANDS: Set<CardBrand> = setOf(CardBrand(txVariant = CardType.BCMC.txVariant))
    }
}
