/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/12/2020.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.internal.ui.model.DropInParams
import com.adyen.checkout.dropin.internal.ui.model.StoredPaymentMethodModel
import com.adyen.checkout.dropin.internal.util.mapStoredModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.Locale

internal class PreselectedStoredPaymentViewModel(
    private val storedPaymentMethod: StoredPaymentMethod,
    private val dropInParams: DropInParams,
) : ViewModel(), ComponentCallback<PaymentComponentState<*>> {

    private val _uiStateFlow = MutableStateFlow(getInitialUIState())
    val uiStateFlow: Flow<PreselectedStoredState> = _uiStateFlow

    private val eventsChannel: Channel<PreselectedStoredEvent> = bufferedChannel()
    val eventsFlow: Flow<PreselectedStoredEvent> = eventsChannel.receiveAsFlow()

    private var componentState: PaymentComponentState<*>? = null

    private fun getInitialUIState(): PreselectedStoredState {
        val storedPaymentMethodModel = storedPaymentMethod.mapStoredModel(
            dropInParams.isRemovingStoredPaymentMethodsEnabled,
            dropInParams.environment,
        )
        return PreselectedStoredState(
            storedPaymentMethodModel = storedPaymentMethodModel,
            buttonState = ButtonState.ContinueButton(),
        )
    }

    override fun onSubmit(state: PaymentComponentState<*>) {
        // no ops
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        error("This event should not be used in drop-in")
    }

    override fun onError(componentError: ComponentError) {
        eventsChannel.trySend(PreselectedStoredEvent.ShowError(componentError))
    }

    override fun onStateChanged(state: PaymentComponentState<*>) {
        componentState = state
        val buttonState = getButtonState(state)
        val newState = _uiStateFlow.value.copy(buttonState = buttonState)
        _uiStateFlow.tryEmit(newState)
    }

    private fun getButtonState(state: PaymentComponentState<*>): ButtonState {
        return if (!state.isInputValid) {
            // component requires user input -> use it as a Continue button
            ButtonState.ContinueButton()
        } else {
            // component does not require user input -> treat it as a normal Pay button
            ButtonState.PayButton(dropInParams.amount, dropInParams.shopperLocale)
        }
    }

    fun onButtonClicked() {
        val componentState = componentState ?: return

        if (!componentState.isInputValid) {
            // component requires user input -> load the stored component in a new fragment
            eventsChannel.trySend(PreselectedStoredEvent.ShowStoredPaymentScreen)
            return
        }

        if (!componentState.isValid) {
            // component is not yet ready for submitting -> show a loading indicator until the component indicates that
            // we can make the payments call (by emitting PaymentComponentEvent.Submit)
            val newState = _uiStateFlow.value.copy(buttonState = ButtonState.Loading)
            _uiStateFlow.tryEmit(newState)
        } else {
            // component does not require user input -> we should submit it directly instead of switching to a new
            // screen
            eventsChannel.trySend(
                PreselectedStoredEvent.ShowConfirmationPopup(
                    storedPaymentMethod.name.orEmpty(),
                    _uiStateFlow.value.storedPaymentMethodModel,
                ),
            )
        }
    }

    fun onConfirmed() {
        val componentState = componentState ?: return
        eventsChannel.trySend(PreselectedStoredEvent.RequestPaymentsCall(componentState))
    }
}

internal data class PreselectedStoredState(
    val storedPaymentMethodModel: StoredPaymentMethodModel,
    val buttonState: ButtonState,
)

internal sealed class ButtonState {
    object Loading : ButtonState()
    data class ContinueButton(@StringRes val labelResId: Int = R.string.continue_button) : ButtonState()
    data class PayButton(val amount: Amount?, val shopperLocale: Locale) : ButtonState()
}

internal sealed class PreselectedStoredEvent {
    data object ShowStoredPaymentScreen : PreselectedStoredEvent()
    data class ShowConfirmationPopup(
        val paymentMethodName: String,
        val storedPaymentMethodModel: StoredPaymentMethodModel
    ) : PreselectedStoredEvent()

    data class RequestPaymentsCall(val state: PaymentComponentState<*>) : PreselectedStoredEvent()
    data class ShowError(val componentError: ComponentError) : PreselectedStoredEvent()
}
