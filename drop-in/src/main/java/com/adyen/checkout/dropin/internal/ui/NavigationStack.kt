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

@Composable
internal fun NavigationStack(
    viewModel: DropInViewModel,
) {
    NavDisplay(
        backStack = viewModel.navigator.backStack,
        sceneStrategies = remember { listOf(BottomSheetSceneStrategy()) },
        onBack = { viewModel.navigator.back() },
        // Without a view model decorator every view model would go into the activity's store and only be cleared when
        // the activity is destroyed, so a payment flow would outlive the screen that started it. The saveable
        // decorator is the NavDisplay default and is required by the view model one, so both have to be listed once
        // this list is passed explicitly. The view model one additionally hands an entry the store of the parent it
        // declares, on top of the one of its own.
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
        viewModel.navigator,
        viewModel(
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
        PaymentMethodScreen(
            navigator = viewModel.navigator,
            viewModel = viewModel(factory = paymentMethodViewModelFactory(key.paymentFlowType, viewModel)),
            theme = viewModel.theme,
        )
    }
}

private fun actionNavEntry(
    key: ActionNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(
    key = key,
    // The action screen replaces the back stack, so it cannot slide back out sideways onto the screen it came from.
    metadata = DropInTransitions.slideInHorizontallyAndOutVertically() +
        SharedViewModelStoreNavEntryDecorator.parent(paymentFlowContentKey(key.paymentFlowType)),
) {
    ActionScreen(
        navigator = viewModel.navigator,
        // Resolved against the parent's store rather than this entry's own, so this is the instance that already owns
        // the flow rather than a second one built from the same factory.
        viewModel = viewModel(
            viewModelStoreOwner = LocalSharedViewModelStoreOwner.current,
            factory = paymentMethodViewModelFactory(key.paymentFlowType, viewModel),
        ),
    )
}

private fun paymentMethodViewModelFactory(
    paymentFlowType: DropInPaymentFlowType,
    viewModel: DropInViewModel,
) = PaymentMethodViewModel.Factory(
    paymentFlowType = paymentFlowType,
    dropInParams = viewModel.dropInParams,
    paymentMethodRepository = viewModel.paymentMethodRepository,
    navigator = viewModel.navigator,
    controllerProvider = viewModel.controllerProvider,
)

/**
 * The content key of the entry owning the flow of [paymentFlowType]. The payment method entry declares it as its own
 * [NavEntry.contentKey] and the action entry names the same value as its parent, which is what points the two at one
 * shared view model store.
 */
private fun paymentFlowContentKey(paymentFlowType: DropInPaymentFlowType): String = paymentFlowType.toString()
