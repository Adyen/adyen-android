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
        // listed once this list is passed explicitly. The view model one scopes a view model to the flow its entry
        // declares through scopeTo, rather than to the entry itself.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberFlowScopedViewModelStoreNavEntryDecorator(),
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
    metadata = DropInTransitions.slideInAndOutVertically() + scopeTo(key.flowKey),
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
        metadata = transitions + scopeTo(key.flowKey),
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
    // Scoping to the flow of the owner is what continues it on the controller that started it.
    metadata = DropInTransitions.slideInHorizontallyAndOutVertically() + scopeTo(key.flowKey),
) {
    val controller = when (key.owner) {
        ActionFlowOwner.PAYMENT_METHOD -> paymentMethodViewModel(key.paymentFlowType, viewModel).controller
        ActionFlowOwner.PAYMENT_METHOD_LIST ->
            paymentMethodListViewModel(viewModel).findExpressPaymentMethodController(key.paymentFlowType)
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
 * Resolves the view model of the payment flow [paymentFlowType] identifies. The payment method screen and the action
 * screen that follows it share a flow key, so the first of them creates it and the other gets that same instance.
 */
@Composable
private fun paymentMethodViewModel(
    paymentFlowType: DropInPaymentFlowType,
    viewModel: DropInViewModel,
): PaymentMethodViewModel = viewModel(
    factory = PaymentMethodViewModel.Factory(
        paymentFlowType = paymentFlowType,
        paymentMethodRepository = viewModel.paymentMethodRepository,
        dropInParams = viewModel.dropInParams,
        navigator = viewModel.navigator,
        controllerProvider = viewModel.controllerProvider,
    ),
)

/**
 * Resolves the view model of the payment method list, which owns the express payment method flows. The list and their
 * action screen share a flow key, so the action screen gets the instance the list created.
 */
@Composable
private fun paymentMethodListViewModel(
    viewModel: DropInViewModel,
): PaymentMethodListViewModel = viewModel(
    factory = PaymentMethodListViewModel.Factory(
        dropInParams = viewModel.dropInParams,
        paymentMethodRepository = viewModel.paymentMethodRepository,
        navigator = viewModel.navigator,
        controllerProvider = viewModel.controllerProvider,
    ),
)
