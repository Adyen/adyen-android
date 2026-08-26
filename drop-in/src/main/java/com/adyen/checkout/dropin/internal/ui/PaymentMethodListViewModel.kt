/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/11/2025.
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
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PayByBankUSPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.PaymentMethodSupportCheck
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import com.adyen.checkout.dropin.internal.ui.PaymentMethodListViewState.PaymentMethodItem
import com.adyen.checkout.dropin.internal.ui.PaymentMethodListViewState.PaymentMethodListSection
import com.adyen.checkout.paybybankus.internal.ui.model.PayByBankUSBrandLogo
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Renders the payment method list, and owns the promoted payment method flows on top of that.
 *
 * Promoted payment methods are the exception to [PaymentMethodViewModel]: their buttons are part of this list rather
 * than a screen behind a list item, so their controllers have to exist before the shopper picks anything.
 * The [ActionNavKey] that follows declares [PaymentMethodListNavKey] as its parent, so it reads this view model back
 * out of the list's store and those flows continue on the same controller even once the list itself is gone.
 */
internal class PaymentMethodListViewModel(
    private val dropInParams: DropInParams,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentMethodSupportCheck: PaymentMethodSupportCheck,
    private val navigator: DropInNavigator,
    controllerProvider: DropInControllerProvider,
) : ViewModel() {

    val viewState: StateFlow<PaymentMethodListViewState> = paymentMethodRepository.storedPaymentMethods
        .map { createViewState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createViewState(emptyList()))

    // Google Pay is the only promoted payment method so far. Turn this into a list once there can be more.
    private val promotedPaymentMethodFlowType: DropInPaymentFlowType? = paymentMethodRepository.paymentMethods
        .firstOrNull { it.type in GOOGLE_PAY_TYPES }
        ?.type
        ?.let { DropInPaymentFlowType.RegularPaymentMethod(it) }

    /**
     * Renders the promoted payment method button, or `null` when none is offered. Created eagerly rather than when the
     * shopper taps it, because the button is drawn by the controller itself.
     */
    val promotedPaymentMethodController: CheckoutController? = promotedPaymentMethodFlowType
        ?.let { controllerProvider.provide(it, viewModelScope) }

    init {
        observePromotedPaymentMethodNavigation()
    }

    /**
     * The controller driving the flow of [paymentFlowType], or `null` when that flow is not one of the promoted payment
     * methods this view model owns.
     */
    fun findPromotedPaymentMethodController(paymentFlowType: DropInPaymentFlowType): CheckoutController? =
        promotedPaymentMethodController.takeIf { promotedPaymentMethodFlowType == paymentFlowType }

    /**
     * Navigates to the promoted payment method action screen when its payments call returns an action.
     *
     * [CheckoutController.navigation] has no replay, so the subscription has to be active before the shopper can tap
     * the button. [CoroutineStart.UNDISPATCHED] makes sure it is set up before this view model is handed out.
     */
    private fun observePromotedPaymentMethodNavigation() {
        val paymentFlowType = promotedPaymentMethodFlowType ?: return
        val controller = promotedPaymentMethodController ?: return

        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            controller.navigation.collect { route ->
                when (route) {
                    // Replacing the back stack means going back from the action cancels Drop-in, matching what the
                    // other payment methods do.
                    is CheckoutRoute.Action -> navigator.clearAndNavigateTo(
                        ActionNavKey(paymentFlowType, ActionFlowOwner.PAYMENT_METHOD_LIST),
                    )

                    else -> Unit
                }
            }
        }
    }

    private fun createViewState(storedPaymentMethods: List<StoredPaymentMethod>): PaymentMethodListViewState {
        val storedPaymentMethodSection = storedPaymentMethods
            .filter { paymentMethodSupportCheck.isSupported(it) }
            .takeIf { it.isNotEmpty() }
            ?.map { it.toPaymentMethodItem() }
            ?.let { paymentMethods ->
                PaymentMethodListSection(
                    title = CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_FAVORITES_SECTION_TITLE,
                    action = CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_FAVORITES_SECTION_ACTION,
                    options = paymentMethods,
                )
            }

        val paymentOptionsSection = paymentMethodRepository.paymentMethods
            // Promoted payment methods are rendered above the list by their own component, so they must not appear as
            // a list item too.
            // TODO - Prototype: it is filtered out even when Google Pay turns out to be unavailable, in which case the
            //  shopper is left with no Google Pay entry at all.
            .filterNot { it.type in GOOGLE_PAY_TYPES }
            // TODO - Check availability for Google Pay and WeChat. If unavailable filter them also out
            .filter { paymentMethodSupportCheck.isSupported(it) }
            .takeIf { it.isNotEmpty() }
            ?.map { it.toPaymentMethodItem() }
            ?.let { paymentMethods ->
                PaymentMethodListSection(
                    title = if (storedPaymentMethodSection == null) {
                        CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_PAYMENT_OPTIONS_SECTION_TITLE
                    } else {
                        CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_PAYMENT_OPTIONS_SECTION_TITLE_WITH_FAVORITES
                    },
                    action = null,
                    options = paymentMethods,
                )
            }

        return PaymentMethodListViewState(
            amount = dropInParams.amount.format(dropInParams.shopperLocale),
            storedPaymentMethodSection = storedPaymentMethodSection,
            paymentOptionsSection = paymentOptionsSection,
        )
    }

    private fun StoredPaymentMethod.toPaymentMethodItem(): PaymentMethodItem {
        val icon = StoredPaymentMethodFormatter.getIcon(this)
        val title = StoredPaymentMethodFormatter.getTitle(this)
        val subtitle = StoredPaymentMethodFormatter.getSubtitle(this)

        return PaymentMethodItem(
            id = id,
            icon = icon,
            title = title,
            subtitle = subtitle,
        )
    }

    private fun PaymentMethod.toPaymentMethodItem(): PaymentMethodItem {
        val icon = when (this) {
            is CardPaymentMethod -> CARD_LOGO
            is GiftCardPaymentMethod -> brand
            else -> type
        }

        val brands = when (this) {
            is CardPaymentMethod -> brands
            is PayByBankUSPaymentMethod -> PayByBankUSBrandLogo.entries.map { it.path }
            else -> null
        }

        return PaymentMethodItem(
            id = type,
            icon = icon,
            title = name,
            brands = brands,
        )
    }

    companion object {
        private const val CARD_LOGO = "card"
    }

    class Factory(
        private val dropInParams: DropInParams,
        private val paymentMethodRepository: PaymentMethodRepository,
        private val navigator: DropInNavigator,
        private val controllerProvider: DropInControllerProvider,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PaymentMethodListViewModel(
                dropInParams = dropInParams,
                paymentMethodRepository = paymentMethodRepository,
                paymentMethodSupportCheck = PaymentMethodSupportCheck(),
                navigator = navigator,
                controllerProvider = controllerProvider,
            ) as T
        }
    }
}
