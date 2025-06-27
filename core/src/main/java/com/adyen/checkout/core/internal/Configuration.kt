/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/5/2025.
 */

package com.adyen.checkout.core.internal

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.analytics.AnalyticsConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.data.model.Amount
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface Configuration : Parcelable {

    val environment: Environment
    val clientKey: String
    val shopperLocale: Locale?
    val analyticsConfiguration: AnalyticsConfiguration?
    val amount: Amount?
}
