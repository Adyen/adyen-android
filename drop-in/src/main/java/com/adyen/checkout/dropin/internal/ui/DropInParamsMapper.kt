/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.dropin.DropInConfiguration

internal class DropInParamsMapper {

    fun mapToParams(params: CheckoutParams): DropInParams {
        val dropInConfiguration = params.getConfiguration<DropInConfiguration>()
        return DropInParams(
            hideStoredPaymentMethods = dropInConfiguration?.hideStoredPaymentMethods ?: false,
            startWithLastStoredPaymentMethod = dropInConfiguration?.startWithLastStoredPaymentMethod ?: true,
        )
    }
}
