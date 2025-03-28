/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.state.StateUpdater
import com.adyen.checkout.components.core.internal.ui.model.state.StateUpdaterRegistry
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel

internal class MBWayStateUpdaterRegistry : StateUpdaterRegistry<MBWayDelegateState, MBWayFieldId> {

    private val updaters = MBWayFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            MBWayFieldId.LOCAL_PHONE_NUMBER -> LocalPhoneNumberUpdater()
            MBWayFieldId.COUNTRY_CODE -> CountryCodeUpdater()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getFieldState(
        state: MBWayDelegateState,
        key: MBWayFieldId,
    ): ComponentFieldDelegateState<T> {
        val updater = updaters[key] as? StateUpdater<MBWayDelegateState, ComponentFieldDelegateState<T>>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.getFieldState(state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> updateFieldState(
        state: MBWayDelegateState,
        key: MBWayFieldId,
        fieldState: ComponentFieldDelegateState<T>,
    ): MBWayDelegateState {
        val updater = updaters[key] as? StateUpdater<MBWayDelegateState, ComponentFieldDelegateState<T>>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.updateFieldState(state, fieldState)
    }
}

internal class LocalPhoneNumberUpdater : StateUpdater<MBWayDelegateState, ComponentFieldDelegateState<String>> {
    override fun getFieldState(state: MBWayDelegateState): ComponentFieldDelegateState<String> =
        state.localPhoneNumberFieldState

    override fun updateFieldState(state: MBWayDelegateState, fieldState: ComponentFieldDelegateState<String>) =
        state.copy(
            localPhoneNumberFieldState = fieldState,
        )
}

internal class CountryCodeUpdater : StateUpdater<MBWayDelegateState, ComponentFieldDelegateState<CountryModel>> {
    override fun getFieldState(state: MBWayDelegateState): ComponentFieldDelegateState<CountryModel> =
        state.countryCodeFieldState

    override fun updateFieldState(state: MBWayDelegateState, fieldState: ComponentFieldDelegateState<CountryModel>) =
        state.copy(
            countryCodeFieldState = fieldState,
        )
}
