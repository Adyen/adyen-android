/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.PaymentMethodFormatter
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the [CheckoutController] that drives the payment flow of a single payment method, on top of the state its
 * screen renders.
 *
 * The controller is created in [viewModelScope], so it lives exactly as long as the [PaymentMethodNavKey] entry this
 * view model belongs to and is torn down when nav3 clears that entry's store.
 *
 * The [ActionNavKey] that follows an action declares that same entry as its parent, so it reads this instance back out
 * of its store rather than being handed the controller. That is what lets the flow survive the navigation from this
 * screen to the action screen, even though the action replaces this screen on the back stack.
 *
 * Serving those two screens is also why there are two states here: they render the same payment method, but not the
 * same parts of it.
 */
internal class PaymentMethodViewModel(
    private val paymentFlowType: DropInPaymentFlowType,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val navigator: DropInNavigator,
    controllerProvider: DropInControllerProvider,
) : ViewModel() {

    val controller: CheckoutController = controllerProvider.provide(paymentFlowType, viewModelScope)

    private val paymentMethod = resolvePaymentMethod()

    private val _paymentMethodViewState = MutableStateFlow(createPaymentMethodViewState())
    val paymentMethodViewState: StateFlow<PaymentMethodViewState> = _paymentMethodViewState.asStateFlow()

    private val _actionViewState = MutableStateFlow(createActionViewState())
    val actionViewState: StateFlow<ActionViewState> = _actionViewState.asStateFlow()

    init {
        observeNavigation()
    }

    /**
     * Navigates to the action screen once the payments call returns an action.
     *
     * [CheckoutController.navigation] has no replay, so the subscription has to be active before anything can be
     * submitted on the controller. [CoroutineStart.UNDISPATCHED] guarantees that by running the collection before the
     * constructor returns.
     */
    private fun observeNavigation() {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            controller.navigation.collect { route ->
                when (route) {
                    is CheckoutRoute.Action -> navigator.clearAndNavigateTo(ActionNavKey(paymentFlowType))
                    else -> Unit
                }
            }
        }
    }

    private fun resolvePaymentMethod(): PaymentMethodResponse {
        val paymentMethod = when (paymentFlowType) {
            is DropInPaymentFlowType.RegularPaymentMethod -> {
                paymentMethodRepository.paymentMethods.firstOrNull { it.type == paymentFlowType.txVariant }
            }

            is DropInPaymentFlowType.StoredPaymentMethod -> {
                paymentMethodRepository.storedPaymentMethods.value.firstOrNull { it.id == paymentFlowType.id }
            }
        }

        // A flow is only ever started for a payment method that is in the repository, so a miss here is a programming
        // error rather than a state to recover from.
        return checkNotNull(paymentMethod) { "No payment method found for $paymentFlowType" }
    }

    private fun createPaymentMethodViewState() = PaymentMethodViewState(
        paymentMethodName = paymentMethod.name,
        description = paymentMethod.getDescription(),
    )

    // TODO - A card resolves to the generic card logo rather than the brand the shopper selected, which is what this
    //  screen should be showing. Either show the brand here, or resolve the logo another way.
    private fun createActionViewState() = ActionViewState(
        logoTxVariant = paymentMethod.getLogoTxVariant(),
        paymentMethodName = paymentMethod.name,
    )

    private fun PaymentMethodResponse.getLogoTxVariant() = when (this) {
        is StoredPaymentMethod -> StoredPaymentMethodFormatter.getIcon(this)
        is PaymentMethod -> PaymentMethodFormatter.getIcon(this)
        else -> type
    }

    // TODO - Update this method once payment method refactor is done.
    //  Verify the localization for stored payment methods.
    private fun PaymentMethodResponse.getDescription(): CheckoutLocalizationKey? {
        return when (type) {
            PaymentMethodTypes.SCHEME -> CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_CARD_DESCRIPTION
            else -> null
        }
    }

    class Factory(
        private val paymentFlowType: DropInPaymentFlowType,
        private val paymentMethodRepository: PaymentMethodRepository,
        private val navigator: DropInNavigator,
        private val controllerProvider: DropInControllerProvider,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PaymentMethodViewModel(
                paymentFlowType = paymentFlowType,
                paymentMethodRepository = paymentMethodRepository,
                navigator = navigator,
                controllerProvider = controllerProvider,
            ) as T
        }
    }
}
