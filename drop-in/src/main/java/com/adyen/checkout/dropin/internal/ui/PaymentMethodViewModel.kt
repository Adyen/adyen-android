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
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Owns the [CheckoutController] that drives the payment flow of a single payment method, on top of the state its
 * screen renders.
 *
 * [PaymentMethodNavKey] and [ActionNavKey] declare the same [paymentFlowContentKey], so they share a view model store
 * and therefore this view model. That is what lets the flow survive the navigation from the payment method screen to
 * the action screen without being handed over, and the store is cleared once the last of those entries leaves the
 * back stack, which cancels [viewModelScope] and tears the controller down with it.
 *
 * Google Pay does not go through here. Its button lives on the payment method list rather than on a screen of its own,
 * so it gets [GooglePayViewModel] instead.
 */
// TODO - The view state never changes, so it could be a plain value instead of a state flow.
internal class PaymentMethodViewModel(
    private val paymentFlowType: DropInPaymentFlowType,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val dropInParams: DropInParams,
    private val navigator: DropInNavigator,
    controllerProvider: DropInControllerProvider,
) : ViewModel() {

    val controller: CheckoutController = controllerProvider.provide(paymentFlowType, viewModelScope)

    /**
     * Whether the shopper has to fill something in before the payment can be submitted. A payment method that does not
     * gets a screen that only reports the progress of the payments call.
     */
    // TODO - This should probably be expanded to accommodate stored payment methods, which have
    //  requiresUserInteraction false but will require user interaction
    val requiresUserInteraction: Boolean = controller.requiresUserInteraction()

    private val _viewState = MutableStateFlow(createViewState())
    val viewState: StateFlow<PaymentMethodViewState> = _viewState.asStateFlow()

    init {
        observeNavigation()

        // Submitted from here rather than from whatever navigated here, so the screen reporting progress is already
        // composed by the time the payments call returns an action and replaces it.
        if (!requiresUserInteraction) {
            controller.submit()
        }
    }

    /**
     * Navigates to the action screen when the payments call returns an action.
     *
     * [CheckoutController.navigation] has no replay and a payment method without UI is submitted in [init], so the
     * subscription has to be active before that submit happens. [CoroutineStart.UNDISPATCHED] guarantees that.
     */
    private fun observeNavigation() {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            controller.navigation.collect { route ->
                when (route) {
                    // Replacing the back stack means going back from the action cancels Drop-in, and makes the stack
                    // identical whether the action came from the list or from the payment method screen.
                    is CheckoutRoute.Action -> navigator.clearAndNavigateTo(ActionNavKey(paymentFlowType))
                    else -> Unit
                }
            }
        }
    }

    private fun createViewState(): PaymentMethodViewState {
        val paymentMethod = when (paymentFlowType) {
            is DropInPaymentFlowType.RegularPaymentMethod -> {
                paymentMethodRepository.paymentMethods.first { it.type == paymentFlowType.txVariant }
            }

            is DropInPaymentFlowType.StoredPaymentMethod -> {
                paymentMethodRepository.storedPaymentMethods.value.first { it.id == paymentFlowType.id }
            }
        }

        return PaymentMethodViewState(
            paymentMethodName = paymentMethod.name,
            description = paymentMethod.getDescription(),
            logo = paymentMethod.getLogo(),
            amount = dropInParams.amount.format(dropInParams.shopperLocale),
        )
    }

    private fun PaymentMethodResponse.getLogo(): String {
        return if (this is StoredPaymentMethod) StoredPaymentMethodFormatter.getIcon(this) else type
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
        private val dropInParams: DropInParams,
        private val navigator: DropInNavigator,
        private val controllerProvider: DropInControllerProvider,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return PaymentMethodViewModel(
                paymentFlowType = paymentFlowType,
                paymentMethodRepository = paymentMethodRepository,
                dropInParams = dropInParams,
                navigator = navigator,
                controllerProvider = controllerProvider,
            ) as T
        }
    }
}
