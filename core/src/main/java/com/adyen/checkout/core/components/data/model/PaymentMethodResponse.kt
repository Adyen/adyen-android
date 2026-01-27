/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/12/2025.
 */

package com.adyen.checkout.core.components.data.model

import androidx.compose.runtime.Immutable
import com.adyen.checkout.core.common.internal.model.ModelObject

/**
 * Parent class for [PaymentMethod] and [StoredPaymentMethod].
 */
// TODO - Payment method models - Remove when newly created models are used.
@Immutable
abstract class PaymentMethodResponse : ModelObject() {

    abstract val type: String

    abstract val name: String
}
