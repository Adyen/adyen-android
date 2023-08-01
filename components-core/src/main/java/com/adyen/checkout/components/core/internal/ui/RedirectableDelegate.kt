/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/8/2023.
 */

package com.adyen.checkout.components.core.internal.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface RedirectableDelegate {

    fun setOnRedirectListener(listener: () -> Unit)
}
