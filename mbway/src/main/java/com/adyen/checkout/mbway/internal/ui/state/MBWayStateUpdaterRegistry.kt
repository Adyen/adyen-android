/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.ComponentFieldState
import com.adyen.checkout.core.components.internal.ui.state.updater.StateUpdater
import com.adyen.checkout.core.components.internal.ui.state.updater.StateUpdaterRegistry

internal class MBWayStateUpdaterRegistry : StateUpdaterRegistry<MBWayComponentState, MBWayFieldId> {

    private val updaters = MBWayFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            MBWayFieldId.COUNTRY_CODE -> CountryCodeUpdater()
            MBWayFieldId.PHONE_NUMBER -> LocalPhoneNumberUpdater()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getFieldState(
        state: MBWayComponentState,
        fieldId: MBWayFieldId,
    ): ComponentFieldState<T> {
        val updater = updaters[fieldId] as? StateUpdater<MBWayComponentState, ComponentFieldState<T>>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.getFieldState(state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> updateFieldState(
        state: MBWayComponentState,
        fieldId: MBWayFieldId,
        fieldState: ComponentFieldState<T>,
    ): MBWayComponentState {
        val updater = updaters[fieldId] as? StateUpdater<MBWayComponentState, ComponentFieldState<T>>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.updateFieldState(state, fieldState)
    }
}

internal class LocalPhoneNumberUpdater : StateUpdater<MBWayComponentState, ComponentFieldState<String>> {
    override fun getFieldState(state: MBWayComponentState): ComponentFieldState<String> =
        state.localPhoneNumberFieldState

    override fun updateFieldState(state: MBWayComponentState, fieldState: ComponentFieldState<String>) =
        state.copy(
            localPhoneNumberFieldState = fieldState,
        )
}

internal class CountryCodeUpdater : StateUpdater<MBWayComponentState, ComponentFieldState<CountryModel>> {
    override fun getFieldState(state: MBWayComponentState): ComponentFieldState<CountryModel> =
        state.countryCodeFieldState

    override fun updateFieldState(state: MBWayComponentState, fieldState: ComponentFieldState<CountryModel>) =
        state.copy(
            countryCodeFieldState = fieldState,
        )
}
