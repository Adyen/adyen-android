/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

internal class MBWayViewStateProducer {

    fun produce(componentState: MBWayComponentState): MBWayViewState {
        return MBWayViewState(
            countries = componentState.countries,
            countryCode = componentState.countryCode,
            phoneNumber = componentState.phoneNumber,
            isLoading = componentState.isLoading,
        )
    }
}
