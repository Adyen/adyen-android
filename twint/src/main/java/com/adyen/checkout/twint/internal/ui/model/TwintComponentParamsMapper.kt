/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2023.
 */

package com.adyen.checkout.twint.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.twint.TwintConfiguration

internal class TwintComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: TwintConfiguration,
        sessionParams: SessionParams?,
    ): TwintComponentParams {
        // TODO: override
        return configuration.mapToParamsInternal()
    }

    private fun TwintConfiguration.mapToParamsInternal(): TwintComponentParams {
        return TwintComponentParams(
            shopperLocale,
            environment,
            clientKey,
            AnalyticsParams(analyticsConfiguration),
            false,
            amount,
        )
    }
}
