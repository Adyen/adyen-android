/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.ui

import com.adyen.checkout.dropin.old.internal.ui.model.DropInParams

internal class DropInConfigDataGenerator {

    fun generate(configuration: DropInParams): Map<String, String> {
        return mapOf(
            "skipPaymentMethodList" to configuration.skipListWhenSinglePaymentMethod.toString(),
            "openFirstStoredPaymentMethod" to configuration.showPreselectedStoredPaymentMethod.toString(),
            "isRemovingStoredPaymentMethodsEnabled" to configuration.isRemovingStoredPaymentMethodsEnabled.toString(),
        )
    }
}
