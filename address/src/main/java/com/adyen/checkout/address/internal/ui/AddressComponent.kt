/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
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
import com.adyen.checkout.address.internal.ui.view.AddressSecondaryContent
import com.adyen.checkout.address.internal.ui.view.AddressSecondaryContentEntry
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.ui.EventComponent
import com.adyen.checkout.core.components.internal.ui.SecondaryNavigationEvent
import com.adyen.checkout.core.components.internal.ui.SecondaryScreenComponent
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

/**
 * A form that collects an address for another component, its host. The host merges [navigation] into its own, passes
 * the screens for which [hasScreen] is true to [SecondaryContent], opens the form with [show] and keeps the address
 * reported through [eventFlow].
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

    // The form is only on screen while it is shown, so this view state often has no collectors. The core viewState()
    // helper stops updating 5 s after the last collector leaves, so the form would reopen with values from before the
    // last show(). The form's text fields report their first value back as input, which would overwrite the address
    // it is shown with. Sharing eagerly keeps the view state current.
    // TODO - Address: Use the core viewState() helper once it cannot go stale without collectors.
    @VisibleForTesting
    internal val viewState: StateFlow<AddressViewState> = componentState
        .map(viewStateProducer::produce)
        .stateIn(coroutineScope, SharingStarted.Eagerly, viewStateProducer.produce(componentState.value))

    @Composable
    override fun SecondaryContent(identifier: String, modifier: Modifier) {
        AddressSecondaryContent(
            identifier = identifier,
            viewStateFlow = viewState,
            onIntent = ::onIntent,
            onCountryClick = ::onCountryClick,
            onStateOrProvinceClick = ::onStateOrProvinceClick,
            onSaveClick = ::confirm,
            modifier = modifier,
        )
    }

    /**
     * Whether [identifier] is one of the address component's own screens, which the host passes to [SecondaryContent].
     */
    fun hasScreen(identifier: String): Boolean = AddressSecondaryContentEntry.fromKey(identifier) != null

    /** Opens the form on [address], or on a new form when null, dropping edits the shopper did not save. */
    fun show(address: AddressModel?) {
        onIntent(AddressIntent.Reset(address))
        navigationChannel.trySend(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.FORM.key))
    }

    internal fun confirm() {
        val currentState = componentState.value
        if (componentStateValidator.isValid(currentState)) {
            eventChannel.trySend(AddressComponentEvent.Confirmed(currentState.toAddressModel()))
            navigationChannel.trySend(SecondaryNavigationEvent.Close)
        } else {
            onIntent(AddressIntent.HighlightValidationErrors)
        }
    }

    internal fun onCountryClick() {
        navigationChannel.trySend(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.COUNTRY_PICKER.key))
    }

    internal fun onStateOrProvinceClick() {
        navigationChannel.trySend(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.STATE_PICKER.key))
    }

    /** @param countryCode The ISO 3166-1 alpha-2 code of the picked country. */
    internal fun onCountrySelected(countryCode: String) {
        onIntent(AddressIntent.UpdateCountry(countryCode))
        navigationChannel.trySend(SecondaryNavigationEvent.Close)
    }

    /** @param stateCode The ISO 3166-2 code of the picked state. */
    internal fun onStateOrProvinceSelected(stateCode: String) {
        onIntent(AddressIntent.UpdateStateOrProvince(stateCode))
        navigationChannel.trySend(SecondaryNavigationEvent.Close)
    }

    private fun onIntent(intent: AddressIntent) {
        componentState.handleIntent(intent)
    }
}
