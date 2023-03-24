/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/3/2023.
 */

package com.adyen.checkout.adyen3ds2.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.core.Environment
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
internal data class Adyen3DS2ComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean,
    override val isCreatedByDropIn: Boolean,
    override val amount: Amount,
    val uiCustomization: UiCustomization?,
) : ComponentParams
