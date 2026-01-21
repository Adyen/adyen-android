/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/1/2026.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.dropin.DropInService

class ExampleV6DropInService : DropInService() {

    override suspend fun onSubmit(): CheckoutResult {
        return CheckoutResult.Finished()
    }
}
