/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2025.
 */

package com.adyen.checkout.dropin

import android.app.Service
import androidx.activity.result.ActivityResultLauncher
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.dropin.internal.DropInResultContract

// TODO - KDocs
class DropInLauncher internal constructor(
    private val activityResultLauncher: ActivityResultLauncher<DropInResultContract.Input>,
) {

    internal fun launch(
        dropInContext: CheckoutContext,
        serviceClass: Class<out DropInService>,
    ) {
        val input = DropInResultContract.Input(
            checkoutContext = dropInContext,
            serviceClass = serviceClass,
        )
        activityResultLauncher.launch(input)
    }
}
