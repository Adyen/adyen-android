/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory

internal class MBWayComponentStateFactory : ComponentStateFactory<MBWayComponentState> {
    override fun createDefaultComponentState(): MBWayComponentState {
        return MBWayComponentState()
    }
}
