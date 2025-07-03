/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URL

/**
 * Identifies which host URL to be used for internal network calls.
 */
@Parcelize
data class Environment internal constructor(
    val checkoutShopperBaseUrl: URL,
    val checkoutAnalyticsBaseUrl: URL
) : Parcelable {

    companion object {

        @JvmField
        val TEST: Environment = Environment(
            checkoutShopperBaseUrl = URL("https://checkoutshopper-test.adyen.com/checkoutshopper/"),
            checkoutAnalyticsBaseUrl = URL("https://checkoutanalytics-test.adyen.com/checkoutanalytics/")
        )

        @JvmField
        val EUROPE: Environment = Environment(
            checkoutShopperBaseUrl = URL("https://checkoutshopper-live.adyen.com/checkoutshopper/"),
            checkoutAnalyticsBaseUrl = URL("https://checkoutanalytics-live.adyen.com/checkoutanalytics/")
        )

        @JvmField
        val UNITED_STATES: Environment = Environment(
            checkoutShopperBaseUrl = URL("https://checkoutshopper-live-us.adyen.com/checkoutshopper/"),
            checkoutAnalyticsBaseUrl = URL("https://checkoutanalytics-live-us.adyen.com/checkoutanalytics/")
        )

        @JvmField
        val AUSTRALIA: Environment = Environment(
            checkoutShopperBaseUrl = URL("https://checkoutshopper-live-au.adyen.com/checkoutshopper/"),
            checkoutAnalyticsBaseUrl = URL("https://checkoutanalytics-live-au.adyen.com/checkoutanalytics/")
        )

        @JvmField
        val INDIA: Environment = Environment(
            checkoutShopperBaseUrl = URL("https://checkoutshopper-live-in.adyen.com/checkoutshopper/"),
            checkoutAnalyticsBaseUrl = URL("https://checkoutanalytics-live-in.adyen.com/checkoutanalytics/")
        )

        @JvmField
        val APSE: Environment = Environment(
            checkoutShopperBaseUrl = URL("https://checkoutshopper-live-apse.adyen.com/checkoutshopper/"),
            checkoutAnalyticsBaseUrl = URL("https://checkoutanalytics-live-apse.adyen.com/checkoutanalytics/")
        )
    }
}
