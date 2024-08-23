/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/8/2024.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.dropin.internal.ui.model.DropInParams

internal class DropInConfigDataGenerator {

    fun generate(configuration: DropInParams): Map<String, String> {
        return mapOf(
            "skipPaymentMethodList" to configuration.skipListWhenSinglePaymentMethod.toString(),
            "openFirstStoredPaymentMethod" to configuration.showPreselectedStoredPaymentMethod.toString(),
            "isRemovingStoredPaymentMethodsEnabled" to configuration.isRemovingStoredPaymentMethodsEnabled.toString(),
        )
    }
}
