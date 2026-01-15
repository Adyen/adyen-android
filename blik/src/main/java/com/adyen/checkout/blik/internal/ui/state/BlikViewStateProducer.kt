/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class BlikViewStateProducer : ViewStateProducer<BlikComponentState, BlikViewState> {

    override fun produce(state: BlikComponentState): BlikViewState {
        return BlikViewState(
            blikCode = state.blikCode.toViewState(),
            isLoading = state.isLoading,
        )
    }
}
