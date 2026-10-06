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
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PayByBankUSPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.PaymentMethodFormatter
import com.adyen.checkout.dropin.internal.helper.PaymentMethodSupportCheck
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import com.adyen.checkout.dropin.internal.ui.PaymentMethodListViewState.PaymentMethodItem
import com.adyen.checkout.dropin.internal.ui.PaymentMethodListViewState.PaymentMethodListSection
import com.adyen.checkout.paybybankus.internal.ui.model.PayByBankUSBrandLogo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class PaymentMethodListViewModel(
    private val checkoutParams: CheckoutParams,
    private val dropInParams: DropInParams,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentMethodSupportCheck: PaymentMethodSupportCheck,
    private val navigator: DropInNavigator,
    controllerProvider: DropInControllerProvider,
) : ViewModel() {

    private val unavailableTypes = MutableStateFlow<Set<String>>(emptySet())

    private val _instantPaymentMethod = MutableStateFlow<InstantPaymentMethod?>(null)
    val instantPaymentMethod: StateFlow<InstantPaymentMethod?> = _instantPaymentMethod.asStateFlow()

    val viewState: StateFlow<PaymentMethodListViewState> = combine(
        paymentMethodRepository.storedPaymentMethods,
        unavailableTypes,
    ) { storedPaymentMethods, unavailable -> createInitialViewState(storedPaymentMethods, unavailable) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            createInitialViewState(emptyList(), emptySet()),
        )

    init {
        checkAvailability(controllerProvider)
    }

    fun instantPaymentMethodFor(paymentFlowType: DropInPaymentFlowType): Flow<InstantPaymentMethod?> =
        instantPaymentMethod.map { it?.takeIf { it.paymentFlowType == paymentFlowType } }

    /**
     * Runs the registered availability check for every candidate in parallel. The instant payment method's
     * controller is only created when its check passes: creating it reports an unavailable method as an error,
     * which Drop-in turns into a failure as soon as the list opens. Unavailable listed methods are removed once
     * the checks resolve.
     */
    private fun checkAvailability(controllerProvider: DropInControllerProvider) {
        val instantPaymentMethod = paymentMethodRepository.paymentMethods
            .firstOrNull { it.type in INSTANT_PAYMENT_METHOD_TYPES }
        val candidates = paymentMethodRepository.paymentMethods
            .filterNot { it.type in INSTANT_PAYMENT_METHOD_TYPES }
            .filter { paymentMethodSupportCheck.isSupported(it) }

        viewModelScope.launch {
            if (instantPaymentMethod != null) {
                launch {
                    if (PaymentMethodProvider.isAvailable(instantPaymentMethod, checkoutParams)) {
                        _instantPaymentMethod.value = createInstantPaymentMethod(
                            controllerProvider,
                            instantPaymentMethod.type,
                        )
                    }
                }
            }

            unavailableTypes.value = candidates
                .map { async { it.type to PaymentMethodProvider.isAvailable(it, checkoutParams) } }
                .awaitAll()
                .filterNot { it.second }
                .mapTo(mutableSetOf()) { it.first }
        }
    }

    private fun createInstantPaymentMethod(
        controllerProvider: DropInControllerProvider,
        type: String,
    ): InstantPaymentMethod {
        val paymentFlowType = DropInPaymentFlowType.RegularPaymentMethod(type)

        return InstantPaymentMethod(
            paymentFlowType = paymentFlowType,
            controller = controllerProvider.provide(
                paymentFlowType = paymentFlowType,
                coroutineScope = viewModelScope,
                onAction = { navigateToAction(paymentFlowType) },
            ),
        )
    }

    private fun navigateToAction(paymentFlowType: DropInPaymentFlowType) {
        navigator.clearAndNavigateTo(ActionNavKey(paymentFlowType, ActionFlowOwner.PAYMENT_METHOD_LIST))
    }

    private fun createInitialViewState(
        storedPaymentMethods: List<StoredPaymentMethod>,
        unavailableTypes: Set<String>,
    ): PaymentMethodListViewState {
        val visibleStoredPaymentMethods = if (dropInParams.hideStoredPaymentMethods) {
            emptyList()
        } else {
            storedPaymentMethods
        }

        val storedPaymentMethodSection = visibleStoredPaymentMethods
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
            // Every instant type is filtered, otherwise both Google Pay types would render one as a button and the
            // other as a list item. After personalize integration, we will get a different flag to use for filtering.
            .filterNot { it.type in INSTANT_PAYMENT_METHOD_TYPES }
            .filter { paymentMethodSupportCheck.isSupported(it) }
            .filterNot { it.type in unavailableTypes }
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
            amount = checkoutParams.amount?.format(checkoutParams.shopperLocale).orEmpty(),
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
        val icon = PaymentMethodFormatter.getIcon(this)

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

    private companion object {
        private val INSTANT_PAYMENT_METHOD_TYPES =
            listOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }

    class Factory(
        private val checkoutParams: CheckoutParams,
        private val dropInParams: DropInParams,
        private val paymentMethodRepository: PaymentMethodRepository,
        private val navigator: DropInNavigator,
        private val controllerProvider: DropInControllerProvider,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PaymentMethodListViewModel(
                checkoutParams = checkoutParams,
                dropInParams = dropInParams,
                paymentMethodRepository = paymentMethodRepository,
                paymentMethodSupportCheck = PaymentMethodSupportCheck(),
                navigator = navigator,
                controllerProvider = controllerProvider,
            ) as T
        }
    }
}
