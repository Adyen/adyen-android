/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/2/2026.
 */

@file:Suppress("TooManyFunctions")

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
                is SecondaryNavKey -> secondaryNavEntry(key, viewModel)
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
    contentKey = PAYMENT_METHOD_LIST_CONTENT_KEY,
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
        contentKey = paymentFlowContentKey(key.paymentFlowType),
        metadata = transitions,
    ) {
        PaymentMethodScreen(
            navigator = viewModel.navigator,
            viewModel = paymentMethodViewModel(key.paymentFlowType, viewModel),
            theme = viewModel.theme,
        )
    }
}

private fun secondaryNavEntry(
    key: SecondaryNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(
    key = key,
    // The entry that owns the flow stays on the back stack below this one, so naming it as the parent resolves the
    // view model already holding the controller rather than building a second one from the same factory.
    metadata = DropInTransitions.slideInAndOutHorizontally() +
        SharedViewModelStoreNavEntryDecorator.parent(paymentFlowContentKey(key.paymentFlowType)),
) {
    val paymentMethodViewModel = paymentMethodViewModel(
        paymentFlowType = key.paymentFlowType,
        viewModel = viewModel,
        viewModelStoreOwner = LocalSharedViewModelStoreOwner.current,
    )

    SecondaryScreen(
        navigator = viewModel.navigator,
        identifier = key.identifier,
        controller = paymentMethodViewModel.controller,
        theme = viewModel.theme,
    )
}

private fun actionNavEntry(
    key: ActionNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> {
    // Naming the owning entry as the parent is what continues the flow on the controller that started it.
    val parentContentKey = when (key.owner) {
        ActionFlowOwner.PAYMENT_METHOD -> paymentFlowContentKey(key.paymentFlowType)
        ActionFlowOwner.PAYMENT_METHOD_LIST -> PAYMENT_METHOD_LIST_CONTENT_KEY
    }

    return NavEntry(
        key = key,
        metadata = DropInTransitions.slideInHorizontallyAndOutVertically() +
            SharedViewModelStoreNavEntryDecorator.parent(parentContentKey),
    ) {
        // Resolved against the parent's store rather than this entry's own, so this is the instance that already owns
        // the flow rather than a second one built from the same factory.
        val parentOwner = LocalSharedViewModelStoreOwner.current

        when (key.owner) {
            ActionFlowOwner.PAYMENT_METHOD -> {
                val paymentMethodViewModel = paymentMethodViewModel(key.paymentFlowType, viewModel, parentOwner)
                ActionScreen(
                    navigator = viewModel.navigator,
                    viewState = paymentMethodViewModel.actionViewState,
                    controller = paymentMethodViewModel.controller,
                )
            }

            ActionFlowOwner.PAYMENT_METHOD_LIST -> {
                val instantPaymentMethod = paymentMethodListViewModel(viewModel, parentOwner)
                    .findInstantPaymentMethod(key.paymentFlowType)

                if (instantPaymentMethod == null) {
                    adyenLog(AdyenLogLevel.ERROR, "actionNavEntry") {
                        "No instant payment method for ${key.paymentFlowType}, the action cannot be displayed."
                    }
                } else {
                    ActionScreen(
                        navigator = viewModel.navigator,
                        viewState = instantPaymentMethod.actionViewState,
                        controller = instantPaymentMethod.controller,
                    )
                }
            }
        }
    }
}

@Composable
private fun paymentMethodViewModel(
    paymentFlowType: DropInPaymentFlowType,
    viewModel: DropInViewModel,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
): PaymentMethodViewModel = viewModel(
    viewModelStoreOwner = viewModelStoreOwner,
    factory = PaymentMethodViewModel.Factory(
        paymentFlowType = paymentFlowType,
        dropInParams = viewModel.dropInParams,
        paymentMethodRepository = viewModel.paymentMethodRepository,
        navigator = viewModel.navigator,
        controllerProvider = viewModel.controllerProvider,
    ),
)

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

/**
 * The content key of the entry owning the flow of [paymentFlowType]. The payment method entry declares it as its own
 * [NavEntry.contentKey] and the action entry names the same value as its parent, which is what points the two at one
 * shared view model store.
 */
private fun paymentFlowContentKey(paymentFlowType: DropInPaymentFlowType): String = paymentFlowType.toString()

/**
 * The [NavEntry.contentKey] of the payment method list entry. Declared explicitly rather than left to nav3's default,
 * because the action screen of an instant payment method names this same value as its parent.
 */
private const val PAYMENT_METHOD_LIST_CONTENT_KEY = "PaymentMethodList"
