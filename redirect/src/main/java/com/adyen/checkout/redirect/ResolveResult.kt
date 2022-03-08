/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */
package com.adyen.checkout.redirect

import android.content.pm.ResolveInfo

internal class ResolveResult(val type: Type, val resolveInfo: ResolveInfo?) {

    enum class Type {
        RESOLVER_ACTIVITY, DEFAULT_BROWSER, APPLICATION, UNKNOWN
    }
}
