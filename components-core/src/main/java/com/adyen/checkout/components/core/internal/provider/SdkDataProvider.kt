/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/10/2025.
 */

package com.adyen.checkout.components.core.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.data.model.sdkData.Analytics
import com.adyen.checkout.components.core.internal.data.model.sdkData.Authentication
import com.adyen.checkout.components.core.internal.data.model.sdkData.DirectSdkDataCreation
import com.adyen.checkout.components.core.internal.data.model.sdkData.SdkData
import com.adyen.checkout.core.internal.data.model.ModelUtils
import java.util.Date

/**
 * A provider that creates an [SdkData] object with the necessary information.
 */
@OptIn(DirectSdkDataCreation::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SdkDataProvider(
    private val analyticsManager: AnalyticsManager
) {

    /**
     * Creates the [SdkData] object.
     *
     * @return The created encoded [String] from [SdkData] object.
     */
    fun createEncodedSdkData() = createEncodedSdkData(threeDS2SdkVersion = null)

    /**
     * Creates the [SdkData] object.
     *
     * @param threeDS2SdkVersion The version of the 3DS2 SDK.
     * @return The created encoded [String] from [SdkData] object.
     */
    fun createEncodedSdkData(threeDS2SdkVersion: String?): String? {
        val sdkData = SdkData(
            schemaVersion = SCHEMA_VERSION,
            analytics = Analytics(
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            ),
            authentication = Authentication(
                threeDS2SdkVersion = threeDS2SdkVersion,
            ),
            createdAt = Date().time,
            supportNativeRedirect = true
        )

        return ModelUtils.serializeAndEncodeOpt(sdkData, SdkData.SERIALIZER)
    }

    companion object {
        private const val SCHEMA_VERSION = 1
    }
}
