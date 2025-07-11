/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.DelegateFieldState
import com.adyen.checkout.core.components.internal.ui.state.updater.StateUpdater
import com.adyen.checkout.core.components.internal.ui.state.updater.StateUpdaterRegistry

internal class MBWayStateUpdaterRegistry : StateUpdaterRegistry<MBWayDelegateState, MBWayFieldId> {

    private val updaters = MBWayFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            MBWayFieldId.COUNTRY_CODE -> CountryCodeUpdater()
            MBWayFieldId.PHONE_NUMBER -> LocalPhoneNumberUpdater()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getFieldState(
        state: MBWayDelegateState,
        fieldId: MBWayFieldId,
    ): DelegateFieldState<T> {
        val updater = updaters[fieldId] as? StateUpdater<MBWayDelegateState, DelegateFieldState<T>>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.getFieldState(state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> updateFieldState(
        state: MBWayDelegateState,
        fieldId: MBWayFieldId,
        fieldState: DelegateFieldState<T>,
    ): MBWayDelegateState {
        val updater = updaters[fieldId] as? StateUpdater<MBWayDelegateState, DelegateFieldState<T>>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.updateFieldState(state, fieldState)
    }
}

internal class LocalPhoneNumberUpdater : StateUpdater<MBWayDelegateState, DelegateFieldState<String>> {
    override fun getFieldState(state: MBWayDelegateState): DelegateFieldState<String> =
        state.localPhoneNumberFieldState

    override fun updateFieldState(state: MBWayDelegateState, fieldState: DelegateFieldState<String>) =
        state.copy(
            localPhoneNumberFieldState = fieldState,
        )
}

internal class CountryCodeUpdater : StateUpdater<MBWayDelegateState, DelegateFieldState<CountryModel>> {
    override fun getFieldState(state: MBWayDelegateState): DelegateFieldState<CountryModel> =
        state.countryCodeFieldState

    override fun updateFieldState(state: MBWayDelegateState, fieldState: DelegateFieldState<CountryModel>) =
        state.copy(
            countryCodeFieldState = fieldState,
        )
}
