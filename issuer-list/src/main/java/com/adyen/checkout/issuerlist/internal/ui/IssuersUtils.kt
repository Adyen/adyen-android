/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 25/8/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui

import com.adyen.checkout.components.core.InputDetail
import com.adyen.checkout.components.core.Issuer
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel

internal fun List<Issuer>.mapToModel(environment: Environment): List<IssuerModel> =
    this.mapNotNull { (id, name, isDisabled) ->
        if (!isDisabled && id != null && name != null) {
            IssuerModel(id, name, environment)
        } else {
            null
        }
    }

internal fun List<InputDetail>?.getLegacyIssuers(environment: Environment): List<IssuerModel> =
    this.orEmpty()
        .flatMap { it.items.orEmpty() }
        .mapNotNull { (id, name) ->
            if (id != null && name != null) {
                IssuerModel(id, name, environment)
            } else {
                null
            }
        }
