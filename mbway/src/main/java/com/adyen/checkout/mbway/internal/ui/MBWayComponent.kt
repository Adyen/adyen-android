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
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.OrderRequest
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ViewStateManager
import com.adyen.checkout.core.components.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.mbway.internal.ui.state.MBWayChangeListener
import com.adyen.checkout.mbway.internal.ui.state.MBWayPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.mbway.internal.ui.view.MbWayComponent
import com.adyen.checkout.ui.internal.ComponentScaffold
import com.adyen.checkout.ui.internal.PayButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class MBWayComponent(
    override val componentParams: ComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val viewStateManager: ViewStateManager<MBWayViewState>,
    // TODO - Order to be passed later
    private val order: OrderRequest? = null,
) : PaymentComponent<MBWayPaymentComponentState>,
    MBWayChangeListener {

    private val eventChannel = bufferedChannel<PaymentComponentEvent<MBWayPaymentComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<MBWayPaymentComponentState>> =
        eventChannel.receiveAsFlow()

    override fun submit() {
        if (viewStateManager.isValid) {
            val paymentComponentState = viewStateManager.state.value.toPaymentComponentState(
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                order = order,
                amount = componentParams.amount,
            )
            eventChannel.trySend(
                PaymentComponentEvent.Submit(paymentComponentState),
            )
        } else {
            viewStateManager.highlightAllFieldValidationErrors()
        }
    }

    // TODO - After card, decide if we should extract this function
    private fun MBWayViewState.toPaymentComponentState(
        checkoutAttemptId: String,
        order: OrderRequest?,
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
            order = order,
            amount = amount,
        )

        return MBWayPaymentComponentState(
            data = paymentComponentData,
            isValid = true,
        )
    }

    override fun setLoading(isLoading: Boolean) {
        viewStateManager.update {
            copy(isLoading = isLoading)
        }
    }

    override fun onCountryChanged(newCountryCode: CountryModel) {
        viewStateManager.update {
            copy(countryCode = newCountryCode)
        }
    }

    override fun onPhoneNumberChanged(newPhoneNumber: String) {
        viewStateManager.update {
            copy(phoneNumber = phoneNumber.updateText(newPhoneNumber))
        }
    }

    override fun onPhoneNumberFocusChanged(hasFocus: Boolean) {
        viewStateManager.update {
            copy(phoneNumber = phoneNumber.updateFocus(hasFocus))
        }
    }

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        val viewState by viewStateManager.state.collectAsStateWithLifecycle()

        ComponentScaffold(
            modifier = modifier,
            disableInteraction = viewState.isLoading,
            footer = {
                PayButton(onClick = ::submit, isLoading = viewState.isLoading)
            },
        ) {
            MbWayComponent(
                viewState = viewState,
                changeListener = this,
            )
        }
    }
}
