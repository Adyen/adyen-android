/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/3/2021.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

class ComponentDialogViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    companion object {
        private val TAG = LogUtil.getTag()
        private const val COMPONENT_FRAGMENT_STATE_KEY = "COMPONENT_FRAGMENT_STATE"
    }

    private fun getComponentFragmentState(): ComponentFragmentState? {
        return savedStateHandle.get<ComponentFragmentState>(COMPONENT_FRAGMENT_STATE_KEY)
    }

    private fun setComponentFragmentState(state: ComponentFragmentState) {
        savedStateHandle[COMPONENT_FRAGMENT_STATE_KEY] = state
    }

    val componentFragmentState: LiveData<ComponentFragmentState> =
        savedStateHandle.getLiveData(COMPONENT_FRAGMENT_STATE_KEY)
    private var componentState: PaymentComponentState<out PaymentMethodDetails>? = null

    fun payButtonClicked() {
        val componentState = componentState
        Logger.v(
            TAG,
            "payButtonClicked - componentState.isInputValid: ${componentState?.isInputValid} - " +
                "componentState.isReady: ${componentState?.isReady}"
        )
        val paymentState = when {
            componentState == null -> ComponentFragmentState.IDLE
            !componentState.isInputValid -> ComponentFragmentState.INVALID_UI
            componentState.isValid -> ComponentFragmentState.PAYMENT_READY
            !componentState.isReady -> ComponentFragmentState.AWAITING_COMPONENT_INITIALIZATION
            else -> ComponentFragmentState.IDLE
        }
        Logger.v(TAG, "payButtonClicked - setting state $paymentState")
        setComponentFragmentState(paymentState)
    }

    fun paymentStarted() {
        Logger.v(TAG, "paymentStarted")
        setComponentFragmentState(ComponentFragmentState.IDLE)
    }

    fun componentStateChanged(
        componentState: PaymentComponentState<out PaymentMethodDetails>?,
        confirmationRequired: Boolean = true
    ) {
        Logger.v(
            TAG,
            "componentStateChanged - componentState.isInputValid: ${componentState?.isInputValid} - " +
                "componentState.isReady: ${componentState?.isReady} - confirmationRequired: $confirmationRequired"
        )
        this.componentState = componentState
        val currentState = getComponentFragmentState()
        if (currentState == ComponentFragmentState.AWAITING_COMPONENT_INITIALIZATION) {
            if (componentState?.isValid == true) setComponentFragmentState(ComponentFragmentState.PAYMENT_READY)
            else setComponentFragmentState(ComponentFragmentState.IDLE)
        } else if (!confirmationRequired && componentState?.isValid == true) {
            setComponentFragmentState(ComponentFragmentState.PAYMENT_READY)
        }
    }
}

enum class ComponentFragmentState {
    IDLE,
    INVALID_UI,
    AWAITING_COMPONENT_INITIALIZATION,
    PAYMENT_READY
}
