/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/2/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui

import com.adyen.checkout.core.internal.ui.state.DelegateStateFactory
import com.adyen.checkout.core.internal.ui.state.model.DelegateFieldState
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId

// TODO Pass some params like componentParams
internal class MBWayDelegateStateFactory : DelegateStateFactory<MBWayDelegateState, MBWayFieldId> {

    override fun createDefaultDelegateState() = MBWayDelegateState(
        countries = SUPPORTED_COUNTRIES,
        countryCodeFieldState = DelegateFieldState(getInitiallySelectedCountry()),
    )

    private fun getInitiallySelectedCountry(): String = SUPPORTED_COUNTRIES.first()

    override fun getFieldIds(): List<MBWayFieldId> = MBWayFieldId.entries

    companion object {
        // TODO Type should be changed to probably CountryModel
        val SUPPORTED_COUNTRIES = listOf("PT (+351)", "ES (+34)")
    }
}
