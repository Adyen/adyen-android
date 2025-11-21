/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.model.Amount
import java.util.Locale

internal data class DropInParams(
    // TODO - add more parameters when needed
    val shopperLocale: Locale,
    val environment: Environment,
    val amount: Amount,
)
