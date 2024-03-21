package com.adyen.checkout.components.core.internal

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.Amount
import com.adyen.checkout.core.AnalyticsConfiguration
import com.adyen.checkout.core.Environment
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface Configuration : Parcelable {

    val environment: Environment
    val clientKey: String
    val shopperLocale: Locale?
    val analyticsConfiguration: AnalyticsConfiguration?
    val amount: Amount?
}
