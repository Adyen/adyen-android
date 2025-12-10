/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.navigation.CheckoutDisplayStrategy
import com.adyen.checkout.core.components.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.mbway.MBWayCountryCodePickerNavigationKey
import com.adyen.checkout.mbway.MBWayMainNavigationKey
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateReducer
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateValidator
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.mbway.internal.ui.state.MBWayPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateProducer
import com.adyen.checkout.mbway.internal.ui.view.CountryCodePicker
import com.adyen.checkout.mbway.internal.ui.view.MbWayComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class MBWayComponent(
    private val componentParams: ComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val componentStateValidator: MBWayComponentStateValidator,
    componentStateReducer: MBWayComponentStateReducer,
    viewStateProducer: MBWayViewStateProducer,
    coroutineScope: CoroutineScope,
) : PaymentComponent<MBWayPaymentComponentState> {

    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        MBWayNavKey to CheckoutNavEntry(MBWayNavKey, MBWayMainNavigationKey) { backStack -> MainScreen(backStack) },

        MBWayCountryCodeNavKey to CheckoutNavEntry(
            MBWayCountryCodeNavKey,
            MBWayCountryCodePickerNavigationKey,
            CheckoutDisplayStrategy.FULL_SCREEN_DIALOG,
        ) { backStack -> CountryCodePickerScreen(backStack) },
    )

    override val navigationStartingPoint: NavKey = MBWayNavKey

    private val eventChannel = bufferedChannel<PaymentComponentEvent<MBWayPaymentComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<MBWayPaymentComponentState>> =
        eventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = MBWayComponentStateFactory(componentParams = componentParams).createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = coroutineScope,
    )

    private val viewState = componentState.viewState(viewStateProducer, coroutineScope)

    override fun submit() {
        if (componentStateValidator.isValid(componentState.value)) {
            val paymentComponentState = viewState.value.toPaymentComponentState(
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                amount = componentParams.amount,
            )
            eventChannel.trySend(
                PaymentComponentEvent.Submit(paymentComponentState),
            )
        } else {
            onIntent(MBWayIntent.HighlightValidationErrors)
        }
    }

    // TODO - After card, decide if we should extract this function
    private fun MBWayViewState.toPaymentComponentState(
        checkoutAttemptId: String,
        amount: Amount?,
    ): MBWayPaymentComponentState {
        val sanitizedPhoneNumber = phoneNumber.text.trimStart('0')
        val telephoneNumber = "${countryCode.callingCode}$sanitizedPhoneNumber"

        val paymentMethod = MBWayPaymentMethod(
            type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = checkoutAttemptId,
            telephoneNumber = telephoneNumber,
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = null,
            amount = amount,
        )

        return MBWayPaymentComponentState(
            data = paymentComponentData,
            isValid = true,
        )
    }

    override fun setLoading(isLoading: Boolean) {
        componentState.handleIntent(MBWayIntent.UpdateLoading(isLoading))
    }

    private fun onIntent(intent: MBWayIntent) {
        componentState.handleIntent(intent)
    }

    @Composable
    private fun MainScreen(backStack: NavBackStack<NavKey>) {
        val viewState by viewState.collectAsStateWithLifecycle()

        MbWayComponent(
            viewState = viewState,
            onSubmitClick = ::submit,
            onCountryCodePickerClick = { backStack.add(MBWayCountryCodeNavKey) },
            onIntent = ::onIntent,
        )
    }

    @Composable
    private fun CountryCodePickerScreen(backStack: NavBackStack<NavKey>) {
        val viewState by viewState.collectAsStateWithLifecycle()

        CountryCodePicker(
            viewState = viewState,
            onItemClick = {
                onIntent(MBWayIntent.UpdateCountry(it))
                backStack.removeLastOrNull()
            },
        )
    }
}
