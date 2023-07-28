/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/7/2023.
 */

package com.adyen.checkout.components.core

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
 * The different configurable levels of analytics.
 */
enum class AnalyticsLevel {
    /**
     * All analytics events, logs and errors are sent from the library.
     */
    ALL,

    /**
     * No analytics are sent from the library.
     */
    NONE,
}
