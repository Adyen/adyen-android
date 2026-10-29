/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/10/2026.
 */

package com.adyen.checkout.card.internal.helper

import android.content.Context
import android.content.pm.ApplicationInfo

internal fun Context.isDebuggable(): Boolean = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
