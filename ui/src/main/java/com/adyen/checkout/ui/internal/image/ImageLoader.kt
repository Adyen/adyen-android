/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/8/2025.
 */

package com.adyen.checkout.ui.internal.image

import android.graphics.Bitmap
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ImageLoader {

    suspend fun load(
        url: String,
        onSuccess: suspend (Bitmap) -> Unit,
        onError: suspend (Throwable) -> Unit
    )
}
