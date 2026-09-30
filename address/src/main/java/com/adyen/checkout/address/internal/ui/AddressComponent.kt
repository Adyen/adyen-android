/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateFactory
import com.adyen.checkout.address.internal.ui.state.AddressComponentStatePostProcessor
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateReducer
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateValidator
import com.adyen.checkout.address.internal.ui.state.AddressIntent
import com.adyen.checkout.address.internal.ui.state.AddressViewState
import com.adyen.checkout.address.internal.ui.state.AddressViewStateProducer
import com.adyen.checkout.address.internal.ui.state.toAddressModel
import com.adyen.checkout.address.internal.ui.view.AddressContent
import com.adyen.checkout.address.internal.ui.view.AddressSecondaryContent
import com.adyen.checkout.address.internal.ui.view.AddressSecondaryContentEntry
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.ui.EventComponent
import com.adyen.checkout.core.components.internal.ui.SecondaryNavigationEvent
import com.adyen.checkout.core.components.internal.ui.SecondaryScreenComponent
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

/**
 * A form that collects an address for another component, its host. It is not a payment component: it never submits,
 * and it is only reachable through its host.
 *
 * - The host decides where [Content] is shown, for example on one of its own secondary screens.
 * - The address component's own screens, such as the country picker, are opened through [navigation]. The host lists
 *   the address component in its [SecondaryScreenComponent.childScreenComponents], so these screens open on the
 *   host's back stack and are rendered by [SecondaryContent].
 * - A confirmed address is reported through [eventFlow]. The host keeps it and passes it back through [prefill] when
 *   the shopper returns to edit it.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AddressComponent internal constructor(
    private val componentStateValidator: AddressComponentStateValidator,
    componentStateFactory: AddressComponentStateFactory,
    componentStateReducer: AddressComponentStateReducer,
    componentStatePostProcessor: AddressComponentStatePostProcessor,
    viewStateProducer: AddressViewStateProducer,
    coroutineScope: CoroutineScope,
) : SecondaryScreenComponent,
    EventComponent<AddressComponentEvent> {

    private val eventChannel = bufferedChannel<AddressComponentEvent>()
    override val eventFlow: Flow<AddressComponentEvent> = eventChannel.receiveAsFlow()

    private val navigationChannel = bufferedChannel<SecondaryNavigationEvent>()
    override val navigation: Flow<SecondaryNavigationEvent> = navigationChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        postProcessor = componentStatePostProcessor,
    )

    // The form is only on screen while its host shows it, so this view state often has no collectors. The core
    // viewState() helper stops updating 5 s after the last collector leaves, so the form would reopen with values
    // from before the last prefill(). The form's text fields report their first value back as input, which would
    // overwrite the prefill. Sharing eagerly keeps the view state current.
    @VisibleForTesting
    internal val viewState: StateFlow<AddressViewState> = componentState
        .map(viewStateProducer::produce)
        .stateIn(coroutineScope, SharingStarted.Eagerly, viewStateProducer.produce(componentState.value))

    @Composable
    fun Content(modifier: Modifier) {
        AddressContent(
            viewStateFlow = viewState,
            onIntent = ::onIntent,
            onCountryPickerClick = ::onCountryPickerClick,
            onConfirmClick = ::confirm,
            modifier = modifier,
        )
    }

    @Composable
    override fun SecondaryContent(identifier: String, modifier: Modifier) {
        AddressSecondaryContent(
            identifier = identifier,
            viewStateFlow = viewState,
            onCountryClick = ::onCountrySelected,
            modifier = modifier,
        )
    }

    /**
     * Replaces the form with [address], or resets it when null. Call it before showing [Content] so that edits the
     * shopper dismissed without confirming are not shown again.
     */
    fun prefill(address: AddressModel?) {
        onIntent(AddressIntent.Prefill(address))
    }

    @VisibleForTesting
    internal fun confirm() {
        val currentState = componentState.value
        if (componentStateValidator.isValid(currentState)) {
            eventChannel.trySend(AddressComponentEvent.Confirmed(currentState.toAddressModel()))
        } else {
            onIntent(AddressIntent.HighlightValidationErrors)
        }
    }

    @VisibleForTesting
    internal fun onCountryPickerClick() {
        navigationChannel.trySend(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.COUNTRY_PICKER))
    }

    @VisibleForTesting
    internal fun onCountrySelected(country: CountryModel) {
        onIntent(AddressIntent.UpdateCountry(country))
        navigationChannel.trySend(SecondaryNavigationEvent.Close)
    }

    private fun onIntent(intent: AddressIntent) {
        componentState.handleIntent(intent)
    }
}
