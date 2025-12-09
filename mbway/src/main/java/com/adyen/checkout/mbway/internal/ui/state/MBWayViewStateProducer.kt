/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer

internal class MBWayViewStateProducer : ViewStateProducer<MBWayComponentState, MBWayViewState> {

    override fun produce(state: MBWayComponentState): MBWayViewState {
        return MBWayViewState(
            countries = state.countries,
            countryCode = state.countryCode,
            phoneNumber = state.phoneNumber,
            isLoading = state.isLoading,
        )
    }
}
