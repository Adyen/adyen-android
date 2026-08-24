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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
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
        // listed once this list is passed explicitly. Together they scope a view model to the content key of its nav
        // entry, so entries sharing a content key share their view models.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key ->
            when (key) {
                is EmptyNavKey -> emptyNavEntry(key)
                is PreselectedPaymentMethodNavKey -> preselectedPaymentMethodNavEntry(key, viewModel)
                is PaymentMethodListNavKey -> paymentMethodListNavEntry(key, viewModel)
                is StoredPaymentMethodsNavKey -> storedPaymentMethodsNavEntry(key, viewModel)
                is PaymentMethodNavKey -> paymentMethodNavEntry(key, viewModel)
                is ActionNavKey -> actionNavEntry(key, viewModel)
                is GooglePayActionNavKey -> googlePayActionNavEntry(key, viewModel)
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
    contentKey = GOOGLE_PAY_FLOW_CONTENT_KEY,
    metadata = DropInTransitions.slideInAndOutVertically(),
) {
    PaymentMethodListScreen(
        navigator = viewModel.navigator,
        // Created here rather than observed, because the button has to be ready before the shopper taps it.
        googlePayController = googlePayViewModel(viewModel)?.controller,
        viewModel = viewModel(
            factory = PaymentMethodListViewModel.Factory(
                dropInParams = viewModel.dropInParams,
                paymentMethodRepository = viewModel.paymentMethodRepository,
            ),
        ),
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
        contentKey = paymentFlowContentKey(key.paymentFlowType),
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
    contentKey = paymentFlowContentKey(key.paymentFlowType),
    // The action screen replaces the back stack, so it cannot slide back out sideways onto the screen it came from.
    metadata = DropInTransitions.slideInHorizontallyAndOutVertically(),
) {
    ActionScreen(
        navigator = viewModel.navigator,
        controller = paymentMethodViewModel(key.paymentFlowType, viewModel).controller,
    )
}

private fun googlePayActionNavEntry(
    key: GooglePayActionNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(
    key = key,
    // Shared with the payment method list, so the flow that started there continues on the same controller.
    contentKey = GOOGLE_PAY_FLOW_CONTENT_KEY,
    // The action screen replaces the back stack, so it cannot slide back out sideways onto the screen it came from.
    metadata = DropInTransitions.slideInHorizontallyAndOutVertically(),
) {
    val googlePayViewModel = googlePayViewModel(viewModel) ?: run {
        adyenLog(AdyenLogLevel.ERROR, "googlePayActionNavEntry") {
            "Google Pay is not available, the ActionScreen cannot be displayed."
        }
        return@NavEntry
    }

    ActionScreen(
        navigator = viewModel.navigator,
        controller = googlePayViewModel.controller,
    )
}

/**
 * Resolves the view model of the payment flow [paymentFlowType] identifies. The payment method screen and the action
 * screen that follows it share a content key, so the first of them creates it and the other gets that same instance.
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
 * Resolves the Google Pay view model, or `null` when Google Pay is not offered. The payment method list and the Google
 * Pay action screen share a content key, so both get the same instance.
 */
@Composable
private fun googlePayViewModel(viewModel: DropInViewModel): GooglePayViewModel? {
    val paymentFlowType = viewModel.googlePayFlowType ?: return null

    return viewModel(
        factory = GooglePayViewModel.Factory(
            paymentFlowType = paymentFlowType,
            navigator = viewModel.navigator,
            controllerProvider = viewModel.controllerProvider,
        ),
    )
}
