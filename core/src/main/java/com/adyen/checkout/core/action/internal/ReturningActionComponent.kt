/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/6/2026.
 */

package com.adyen.checkout.core.action.internal

import android.content.Intent
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ReturningActionComponent {
    fun handleReturn(intent: Intent)
}
