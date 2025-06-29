/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Class that allows configuring internal analytics.
 */
@Parcelize
data class AnalyticsConfiguration(
    /**
     * The level of analytics that the library can perform.
     *
     * Default is [AnalyticsLevel.ALL].
     */
    val level: AnalyticsLevel? = null,
) : Parcelable

/**
 * The different configurable levels of analytics. Learn more about the
 * [data we are collecting](https://docs.adyen.com/online-payments/analytics-and-data-tracking/#data-we-are-collecting).
 */
enum class AnalyticsLevel {
    /**
     * All analytics events, logs and errors are sent from the library.
     */
    ALL,

    /**
     * Only Drop-in/Components analytics are not sent from the library.
     */
    NONE,
}
