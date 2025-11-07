/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 8/2/2024.
 */

package com.adyen.checkout.dropin.old.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory

internal object DropInOverrideParamsFactory {
    fun create(amount: Amount?, sessionDetails: SessionDetails?): DropInOverrideParams {
        return DropInOverrideParams(
            amount = amount,
            sessionParams = sessionDetails?.let { SessionParamsFactory.create(sessionDetails) },
        )
    }
}
