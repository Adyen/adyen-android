/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/1/2023.
 */

package com.adyen.checkout.example.ui.bacs

import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.components.base.ComponentCallback
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod

internal data class BacsComponentData(
    val paymentMethod: PaymentMethod,
    val callback: ComponentCallback<BacsDirectDebitComponentState>,
)
