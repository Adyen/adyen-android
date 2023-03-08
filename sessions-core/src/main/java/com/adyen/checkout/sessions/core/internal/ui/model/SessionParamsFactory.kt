/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/3/2023.
 */

package com.adyen.checkout.sessions.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentOptionsParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionSetupConfiguration
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object SessionParamsFactory {
    fun create(checkoutSession: CheckoutSession): SessionParams {
        return create(checkoutSession.sessionSetupResponse.configuration)
    }

    fun create(sessionDetails: SessionDetails): SessionParams {
        return create(sessionDetails.sessionSetupConfiguration)
    }

    private fun create(sessionSetupConfiguration: SessionSetupConfiguration?): SessionParams {
        return SessionParams(
            enableStoreDetails = sessionSetupConfiguration?.enableStoreDetails,
            installmentOptions = sessionSetupConfiguration?.installmentOptions?.map {
                it.key to SessionInstallmentOptionsParams(
                    plans = it.value?.plans,
                    preselectedValue = it.value?.preselectedValue,
                    values = it.value?.values,
                )
            }?.toMap(),
        )
    }
}
