/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/3/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.Environment

internal sealed class BrandState {
    object Placeholder : BrandState()
    data class SingleBrand(val detectedCardType: DetectedCardType, val environment: Environment) : BrandState()
    data class DualBrand(val dualBrandData: DualBrandData, val environment: Environment) : BrandState()
}
