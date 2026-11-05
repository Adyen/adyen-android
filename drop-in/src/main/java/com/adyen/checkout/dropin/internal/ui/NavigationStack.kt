/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/2/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.dropin.internal.manage.StoredPaymentMethodManagementScreen
import com.adyen.checkout.dropin.internal.manage.StoredPaymentMethodManagementViewModel

@Composable
internal fun NavigationStack(
    viewModel: DropInViewModel,
) {
    val navigator = viewModel.navigator
    // The activity has no open transition of its own, so the first frame only shows the dismissed entry. Adding the
    // back stack after it makes NavDisplay animate the first screen in, the same way it animates it out. Saved, so
    // this does not happen again when the activity is recreated.
    var isBackStackShown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { isBackStackShown = true }

    val entries = rememberDecoratedNavEntries(
        // NavDisplay only handles back, and animates it, when there is an entry to go back to. The dismissed entry is
        // that entry for the first screen.
        backStack = if (isBackStackShown) listOf(DismissedNavKey) + navigator.backStack else listOf(DismissedNavKey),
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
                is DismissedNavKey -> dismissedNavEntry(key, viewModel)
                is PreselectedPaymentMethodNavKey -> preselectedPaymentMethodNavEntry(key, viewModel)
                is PaymentMethodListNavKey -> paymentMethodListNavEntry(key, viewModel)
                is StoredPaymentMethodsNavKey -> storedPaymentMethodsNavEntry(key, viewModel)
                is PaymentMethodNavKey -> paymentMethodNavEntry(key, viewModel)
                is ActionNavKey -> actionNavEntry(key, viewModel)
                else -> error("Unknown key: $key")
            }
        },
    )

    // With only the dismissed entry shown, NavDisplay does not handle back, so the activity would finish before Drop-in
    // sends its result. Back does nothing until Drop-in is dismissed instead.
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = !isBackStackShown || navigator.backStack.isEmpty(),
        onBackCompleted = {},
    )

    NavDisplay(
        entries = entries,
        onBack = { navigator.back() },
    )
}

/** Only exists in the UI, so the navigator and the persisted back stack never contain it. */
private data object DismissedNavKey : NavKey

/**
 * Shown on the first frame and once the back stack is empty. NavDisplay only resumes an entry once its transition has
 * settled, so this entry being resumed with an empty back stack means the last screen has finished animating away and
 * Drop-in can close.
 */
private fun dismissedNavEntry(
    key: DismissedNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(key) {
    LifecycleResumeEffect(Unit) {
        viewModel.onDismissed()
        onPauseOrDispose {}
    }
}

private fun preselectedPaymentMethodNavEntry(
    key: PreselectedPaymentMethodNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> = NavEntry(
    key = key,
    // The sheet animates itself in, and hides itself before it is taken off the back stack
    metadata = NavDisplay.transitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
        NavDisplay.popTransitionSpec { EnterTransition.None togetherWith ExitTransition.None },
) {
    PreselectedPaymentMethodScreen(
        viewModel(
            factory = PreselectedPaymentMethodViewModel.Factory(
                checkoutParams = viewModel.checkoutParams,
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
    // The list is only ever the first entry, either from the start or replacing the preselected bottom sheet
    metadata = DropInTransitions.slideUpOverPrevious(),
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
    StoredPaymentMethodManagementScreen(
        navigator = viewModel.navigator,
        viewModel = viewModel(
            factory = StoredPaymentMethodManagementViewModel.Factory(
                paymentMethodRepository = viewModel.paymentMethodRepository,
                analyticsManager = viewModel.analyticsManager,
            ),
        ),
    )
}

private fun paymentMethodNavEntry(
    key: PaymentMethodNavKey,
    viewModel: DropInViewModel,
): NavEntry<NavKey> {
    val navigator = viewModel.navigator
    val transitions = when {
        navigator.isEmptyAfterCurrent() -> DropInTransitions.slideInAndOutVertically()
        navigator.keyBelowCurrent() is PreselectedPaymentMethodNavKey -> DropInTransitions.slideUpOverPrevious()
        else -> DropInTransitions.slideInAndOutHorizontally()
    }

    return NavEntry(
        key = key,
        contentKey = paymentFlowContentKey(key.paymentFlowType),
        metadata = transitions,
    ) {
        PaymentMethodScreen(
            navigator = viewModel.navigator,
            viewModel = paymentMethodViewModel(key.paymentFlowType, viewModel),
        )
    }
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
                    controller = paymentMethodViewModel.controller,
                )
            }

            ActionFlowOwner.PAYMENT_METHOD_LIST -> {
                val paymentMethodListViewModel = paymentMethodListViewModel(viewModel, parentOwner)
                // Observed rather than read once, because the instant payment method is only created after its
                // availability check passes. When the list view model is recreated, it is null until then.
                // TODO - Test this case as part of state restoration, the recreated controller does not continue the
                //  action yet.
                val instantPaymentMethod = paymentMethodListViewModel.instantPaymentMethod
                    .collectAsStateWithLifecycle().value
                    ?.takeIf { it.paymentFlowType == key.paymentFlowType }

                if (instantPaymentMethod == null) {
                    adyenLog(AdyenLogLevel.DEBUG, "actionNavEntry") {
                        "No instant payment method for ${key.paymentFlowType}, the action cannot be displayed."
                    }
                } else {
                    ActionScreen(
                        navigator = viewModel.navigator,
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
        checkoutParams = viewModel.checkoutParams,
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
        checkoutParams = viewModel.checkoutParams,
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
