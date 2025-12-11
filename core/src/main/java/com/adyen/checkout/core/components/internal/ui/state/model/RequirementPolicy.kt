/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 11/12/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class RequirementPolicy {
    data class Required(val label: CheckoutLocalizationKey) : RequirementPolicy()
    data class Optional(val label: CheckoutLocalizationKey) : RequirementPolicy()
    data object Hidden : RequirementPolicy()
}
