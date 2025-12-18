/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/10/2025.
 */

package com.adyen.checkout.components.core.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.data.model.sdkData.Analytics
import com.adyen.checkout.components.core.internal.data.model.sdkData.Authentication
import com.adyen.checkout.components.core.internal.data.model.sdkData.DirectSdkDataCreation
import com.adyen.checkout.components.core.internal.data.model.sdkData.SdkData
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import org.json.JSONException
import java.util.Date
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Default implementation of [SdkDataProvider] that creates an [SdkData] object with the necessary information.
 */
@OptIn(DirectSdkDataCreation::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultSdkDataProvider(
    private val analyticsManager: AnalyticsManager
) : SdkDataProvider {

    @OptIn(ExperimentalEncodingApi::class)
    override fun createEncodedSdkData(threeDS2SdkVersion: String?): String? {
        val sdkData = createSdkData(threeDS2SdkVersion)

        try {
            val jsonObject = sdkData.serialize()
            return Base64.encode(jsonObject.toString().toByteArray(Charsets.UTF_8))
        } catch (e: JSONException) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Unable to serialize SdkData" }
            return null
        }
    }

    private fun createSdkData(threeDS2SdkVersion: String? = null): SdkData {
        val authentication = threeDS2SdkVersion?.let {
            Authentication(
                threeDS2SdkVersion = it,
            )
        }

        return SdkData(
            schemaVersion = SCHEMA_VERSION,
            analytics = Analytics(
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            ),
            authentication = authentication,
            createdAt = Date().time,
            supportNativeRedirect = true,
        )
    }

    companion object {
        private const val SCHEMA_VERSION = 1
    }
}
