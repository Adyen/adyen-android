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
import com.adyen.checkout.components.bundle.SavedStateHandleContainer
import com.adyen.checkout.components.bundle.SavedStateHandleProperty
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

internal class ComponentDialogViewModel(
    override val savedStateHandle: SavedStateHandle
) : ViewModel(), SavedStateHandleContainer {
    companion object {
        private val TAG = LogUtil.getTag()
        private const val COMPONENT_FRAGMENT_STATE_KEY = "COMPONENT_FRAGMENT_STATE"
    }

    private var componentFragmentState: ComponentFragmentState?
        by SavedStateHandleProperty(COMPONENT_FRAGMENT_STATE_KEY)

    val componentFragmentStateLiveData: LiveData<ComponentFragmentState> =
        savedStateHandle.getLiveData(COMPONENT_FRAGMENT_STATE_KEY)
    var componentState: PaymentComponentState<out PaymentMethodDetails>? = null
        private set

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
        componentFragmentState = paymentState
    }

    fun paymentStarted() {
        Logger.v(TAG, "paymentStarted")
        componentFragmentState = ComponentFragmentState.IDLE
    }

    fun componentStateChanged(
        componentState: PaymentComponentState<out PaymentMethodDetails>,
        confirmationRequired: Boolean
    ) {
        Logger.v(
            TAG,
            "componentStateChanged - componentState.isInputValid: ${componentState.isInputValid} - " +
                "componentState.isReady: ${componentState.isReady} - confirmationRequired: $confirmationRequired"
        )
        this.componentState = componentState
        val currentState = componentFragmentState
        if (currentState == ComponentFragmentState.AWAITING_COMPONENT_INITIALIZATION) {
            componentFragmentState = if (componentState.isValid) {
                ComponentFragmentState.PAYMENT_READY
            } else {
                ComponentFragmentState.IDLE
            }
        } else if (!confirmationRequired && componentState.isValid) {
            componentFragmentState = ComponentFragmentState.PAYMENT_READY
        }
    }
}

enum class ComponentFragmentState {
    IDLE,
    INVALID_UI,
    AWAITING_COMPONENT_INITIALIZATION,
    PAYMENT_READY
}
