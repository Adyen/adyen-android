/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/2/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog

@Composable
internal fun NavigationStack(
    viewModel: DropInViewModel,
) {
    NavDisplay(
        backStack = viewModel.navigator.backStack,
        sceneStrategies = remember { listOf(BottomSheetSceneStrategy()) },
        onBack = { viewModel.navigator.back() },
        // The saveable decorator is the NavDisplay default and is required by the view model one, so both have to be
        // listed once this list is passed explicitly. The view model one additionally hands an entry the store of the
        // parent it declares, on top of the one of its own.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberSharedViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key ->
            when (key) {
                is EmptyNavKey -> emptyNavEntry(key)
                is PreselectedPaymentMethodNavKey -> preselectedPaymentMethodNavEntry(key, viewModel)
                is PaymentMethodListNavKey -> paymentMethodListNavEntry(key, viewModel)
                is StoredPaymentMethodsNavKey -> storedPaymentMethodsNavEntry(key, viewModel)
                is PaymentMethodNavKey -> paymentMethodNavEntry(key, viewModel)
                is ActionNavKey -> actionNavEntry(key, viewModel)
                else -> error("Unknown key: $key")
            }
        },
    )
}

// This empty entry makes sure a bottom sheet can be rendered on top of nothing
private fun emptyNavEntry(key: EmptyNavKey): NavEntry<NavKey> = NavEntry(key) {}

@OptIn(ExperimentalMaterial3Api::class)
private fun preselectedPaymentMethodNavEntry(
    key: PreselectedPaymentMethodNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(
    key = key,
    metadata = BottomSheetSceneStrategy.bottomSheet(),
) {
    PreselectedPaymentMethodScreen(
        viewModel(
            factory = PreselectedPaymentMethodViewModel.Factory(
                dropInParams = viewModel.dropInParams,
                storedPaymentMethodId = key.storedPaymentMethodId,
                paymentMethodRepository = viewModel.paymentMethodRepository,
                navigator = viewModel.navigator,
            ),
        ),
    )
}

private fun paymentMethodListNavEntry(
    key: PaymentMethodListNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(
    key = key,
    metadata = DropInTransitions.slideInAndOutVertically(),
) {
    PaymentMethodListScreen(
        navigator = viewModel.navigator,
        viewModel = paymentMethodListViewModel(viewModel),
    )
}

private fun storedPaymentMethodsNavEntry(
    key: StoredPaymentMethodsNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(
    key = key,
    metadata = DropInTransitions.slideInAndOutHorizontally(),
) {
    StoredPaymentMethodsScreen(
        navigator = viewModel.navigator,
        viewModel = viewModel(
            factory = StoredPaymentMethodsViewModel.Factory(
                paymentMethodRepository = viewModel.paymentMethodRepository,
            ),
        ),
    )
}

private fun paymentMethodNavEntry(
    key: PaymentMethodNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> {
    val transitions = if (viewModel.navigator.isEmptyAfterCurrent()) {
        DropInTransitions.slideInAndOutVertically()
    } else {
        DropInTransitions.slideInAndOutHorizontally()
    }

    return NavEntry(
        key = key,
        metadata = transitions,
    ) {
        val paymentMethodViewModel = paymentMethodViewModel(key.paymentFlowType, viewModel)

        if (paymentMethodViewModel.requiresUserInteraction) {
            PaymentMethodScreen(
                navigator = viewModel.navigator,
                controller = paymentMethodViewModel.controller,
                viewModel = paymentMethodViewModel,
            )
        } else {
            // No controller is rendered here: the payments call is already running and this screen reports its
            // progress by rendering the payment method itself rather than a component.
            NoUiPaymentMethodScreen(
                navigator = viewModel.navigator,
                viewModel = paymentMethodViewModel,
            )
        }
    }
}

private fun actionNavEntry(
    key: ActionNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(
    key = key,
    // The action screen replaces the back stack, so it cannot slide back out sideways onto the screen it came from.
    // Declaring the owner as the parent is what continues it on the controller that started it.
    metadata = DropInTransitions.slideInHorizontallyAndOutVertically() +
        SharedViewModelStoreNavEntryDecorator.parent(key.parentContentKey),
) {
    // The controller belongs to the entry this one declared as its parent, so it is read from that entry's store.
    val parentOwner = LocalSharedViewModelStoreOwner.current
    val controller = when (key.owner) {
        ActionFlowOwner.PAYMENT_METHOD ->
            paymentMethodViewModel(key.paymentFlowType, viewModel, parentOwner).controller
        ActionFlowOwner.PAYMENT_METHOD_LIST ->
            paymentMethodListViewModel(viewModel, parentOwner).findPromotedPaymentMethodController(key.paymentFlowType)
    } ?: run {
        adyenLog(AdyenLogLevel.ERROR, "actionNavEntry") {
            "No controller for ${key.paymentFlowType} on ${key.owner}, the ActionScreen cannot be displayed."
        }
        return@NavEntry
    }

    ActionScreen(
        navigator = viewModel.navigator,
        controller = controller,
    )
}

/**
 * Resolves the view model of the payment flow [paymentFlowType] identifies. The payment method screen owns it, and the
 * action screen that follows reads it back out of that screen's store by passing it as [viewModelStoreOwner].
 */
@Composable
private fun paymentMethodViewModel(
    paymentFlowType: DropInPaymentFlowType,
    viewModel: DropInViewModel,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
): PaymentMethodViewModel = viewModel(
    viewModelStoreOwner = viewModelStoreOwner,
    factory = PaymentMethodViewModel.Factory(
        paymentFlowType = paymentFlowType,
        paymentMethodRepository = viewModel.paymentMethodRepository,
        dropInParams = viewModel.dropInParams,
        navigator = viewModel.navigator,
        controllerProvider = viewModel.controllerProvider,
    ),
)

/**
 * Resolves the view model of the payment method list, which owns the promoted payment method flows. Their action screen
 * reads it back out of the list's store by passing it as [viewModelStoreOwner].
 */
@Composable
private fun paymentMethodListViewModel(
    viewModel: DropInViewModel,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
): PaymentMethodListViewModel = viewModel(
    viewModelStoreOwner = viewModelStoreOwner,
    factory = PaymentMethodListViewModel.Factory(
        dropInParams = viewModel.dropInParams,
        paymentMethodRepository = viewModel.paymentMethodRepository,
        navigator = viewModel.navigator,
        controllerProvider = viewModel.controllerProvider,
    ),
)
