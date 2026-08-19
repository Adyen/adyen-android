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
import androidx.navigation3.ui.NavDisplay
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog

@Composable
internal fun NavigationStack(
    viewModel: DropInViewModel,
) {
    // TODO - Investigate scoping view models to their nav entry with rememberViewModelStoreNavEntryDecorator. It
    //  requires the lifecycle-viewmodel-navigation3 dependency and clears a view model once its entry left the
    //  composition, which makes the key below obsolete and would allow screens to get the controller from their own
    //  view model.
    NavDisplay(
        backStack = viewModel.navigator.backStack,
        sceneStrategies = remember { listOf(BottomSheetSceneStrategy()) },
        onBack = { viewModel.navigator.back() },
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
                paymentFlowCoordinator = viewModel.paymentFlowCoordinator,
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
        paymentFlowCoordinator = viewModel.paymentFlowCoordinator,
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
        metadata = transitions,
    ) {
        // The controller is intentionally only read once, so this screen keeps rendering while it is navigated away
        // from and its flow is being torn down.
        val controller = remember(key) { viewModel.paymentFlowCoordinator.activeController } ?: run {
            adyenLog(AdyenLogLevel.ERROR, "paymentMethodNavEntry") {
                "No active payment flow, the PaymentMethodScreen cannot be displayed."
            }
            return@NavEntry
        }

        PaymentMethodScreen(
            navigator = viewModel.navigator,
            controller = controller,
            viewModel = viewModel(
                factory = PaymentMethodViewModel.Factory(
                    paymentFlowType = key.paymentFlowType,
                    paymentMethodRepository = viewModel.paymentMethodRepository,
                ),
                // View models are scoped to the activity, so each payment method needs a unique key of its own.
                // TODO - Remove this key when view models are scoped to their nav entry instead.
                key = key.toString(),
            ),
        )
    }
}

private fun actionNavEntry(
    key: ActionNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(
    key = key,
    // The action screen replaces the back stack, so it cannot slide back out sideways onto the screen it came from.
    metadata = DropInTransitions.slideInHorizontallyAndOutVertically(),
) {
    // The controller is intentionally only read once, so this screen keeps rendering while it is navigated away from
    // and its flow is being torn down.
    val controller = remember(key) { viewModel.paymentFlowCoordinator.activeController } ?: run {
        adyenLog(AdyenLogLevel.ERROR, "actionNavEntry") {
            "No active payment flow, the ActionScreen cannot be displayed."
        }
        return@NavEntry
    }

    ActionScreen(
        navigator = viewModel.navigator,
        controller = controller,
    )
}
