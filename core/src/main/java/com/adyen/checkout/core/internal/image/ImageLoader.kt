/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/8/2025.
 */

package com.adyen.checkout.core.internal.image

import android.graphics.Bitmap

internal interface ImageLoader {

    suspend fun load(url: String): Result<Bitmap>
}
