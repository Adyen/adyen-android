/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/5/2024.
 */

package com.adyen.checkout.ideal.internal.ui

import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.ideal.IdealComponentState
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface IdealDelegate :
    PaymentComponentDelegate<IdealComponentState>,
    ViewProvidingDelegate {

    val componentStateFlow: Flow<IdealComponentState>
}
