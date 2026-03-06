/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.mbway.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.components.internal.PaymentComponentProvider
import com.adyen.checkout.mbway.internal.ui.view.MBWayComponent

internal class MBWayProvider : PaymentComponentProvider {

    @Composable
    override fun PaymentComponent(modifier: Modifier) {
        MBWayComponent(modifier)
    }
}
