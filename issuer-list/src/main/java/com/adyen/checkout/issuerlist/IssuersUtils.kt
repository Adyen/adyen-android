/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 25/8/2022.
 */

package com.adyen.checkout.issuerlist

import com.adyen.checkout.components.model.paymentmethods.InputDetail
import com.adyen.checkout.components.model.paymentmethods.Issuer

object IssuersUtils {

    fun getIssuers(issuerList: List<Issuer>): List<IssuerModel> =
        issuerList.mapNotNull { issuer ->
            val (id, name, isDisabled) = issuer
            if (!isDisabled && id != null && name != null) {
                IssuerModel(id, name)
            } else {
                null
            }
        }

    fun getLegacyIssuers(details: List<InputDetail>?): List<IssuerModel> =
        details.orEmpty()
            .flatMap { it.items.orEmpty() }
            .mapNotNull { item ->
                val (id, name) = item
                if (id != null && name != null) {
                    IssuerModel(id, name)
                } else {
                    null
                }
            }
}
