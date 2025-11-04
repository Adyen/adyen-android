/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/11/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal data object MBWayNavKey : NavKey

@Serializable
internal data object MBWayCountryCodeNavKey : NavKey
