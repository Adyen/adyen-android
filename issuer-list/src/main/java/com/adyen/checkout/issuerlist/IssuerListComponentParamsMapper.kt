/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.issuerlist

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.base.Configuration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class IssuerListComponentParamsMapper(
    private val parentConfiguration: Configuration?,
    private val hideIssuerLogosDefaultValue: Boolean = false
) {

    fun mapToParams(
        issuerListConfiguration: IssuerListConfiguration
    ): IssuerListComponentParams {
        return mapToParams(
            parentConfiguration = parentConfiguration ?: issuerListConfiguration,
            issuerListConfiguration = issuerListConfiguration,
        )
    }

    private fun mapToParams(
        parentConfiguration: Configuration,
        issuerListConfiguration: IssuerListConfiguration,
    ): IssuerListComponentParams {
        with(issuerListConfiguration) {
            return IssuerListComponentParams(
                shopperLocale = parentConfiguration.shopperLocale,
                environment = parentConfiguration.environment,
                clientKey = parentConfiguration.clientKey,
                viewType = viewType ?: IssuerListViewType.RECYCLER_VIEW,
                hideIssuerLogos = hideIssuerLogos ?: hideIssuerLogosDefaultValue,
            )
        }
    }
}
