/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/2/2023.
 */

package com.adyen.checkout.components.util

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Fragment.requireApplication(): Application = requireContext().applicationContext as Application
