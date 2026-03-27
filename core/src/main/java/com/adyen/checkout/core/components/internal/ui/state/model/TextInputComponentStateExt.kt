/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 27/3/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputComponentState.getPaymentDataValue(): String? {
    return text.takeIf { requirementPolicy != RequirementPolicy.Hidden && it.isNotBlank() }
}
