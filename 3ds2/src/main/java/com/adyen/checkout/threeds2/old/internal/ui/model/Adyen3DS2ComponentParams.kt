/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/12/2025.
 */

package com.adyen.checkout.threeds2.old.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.threeds2.customization.UiCustomization

internal data class Adyen3DS2ComponentParams(
    private val commonComponentParams: CommonComponentParams,
    val uiCustomization: UiCustomization?,
    val threeDSRequestorAppURL: String?,
    val deviceParameterBlockList: Set<String>?,
) : ComponentParams by commonComponentParams
