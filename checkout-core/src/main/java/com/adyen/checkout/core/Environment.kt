/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.net.URL
import java.util.Objects

/**
 * Identifies which host URL to be used for network calls.
 */
@Parcelize
class Environment(private val internalUrl: URL) : Parcelable {

    @IgnoredOnParcel
    val baseUrl: String = internalUrl.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is Environment) {
            return false
        }
        return internalUrl.toString() == other.internalUrl.toString()
    }

    override fun hashCode(): Int {
        return Objects.hash(internalUrl)
    }

    companion object {

        @JvmField
        val TEST: Environment = Environment(URL("https://checkoutshopper-test.adyen.com/checkoutshopper/"))

        @JvmField
        val EUROPE: Environment = Environment(URL("https://checkoutshopper-live.adyen.com/checkoutshopper/"))

        @JvmField
        val UNITED_STATES: Environment = Environment(URL("https://checkoutshopper-live-us.adyen.com/checkoutshopper/"))

        @JvmField
        val AUSTRALIA: Environment = Environment(URL("https://checkoutshopper-live-au.adyen.com/checkoutshopper/"))

        @JvmField
        val INDIA: Environment = Environment(URL("https://checkoutshopper-live-in.adyen.com/checkoutshopper/"))

        @JvmField
        val APSE: Environment = Environment(URL("https://checkoutshopper-live-apse.adyen.com/checkoutshopper/"))
    }
}
