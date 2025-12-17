/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/12/2025.
 */

package com.adyen.checkout.core.components.internal.data.provider

import androidx.annotation.RestrictTo

/**
 * A provider that creates SDK data with the necessary information.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface SdkDataProvider {

    /**
     * Creates the encoded SDK data.
     *
     * @param threeDS2SdkVersion The version of the 3DS2 SDK.
     * @return The created encoded [String] from SDK data object.
     */
    fun createEncodedSdkData(threeDS2SdkVersion: String? = null): String?
}
