package com.adyen.checkout.components.core.internal

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.api.Environment
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface Configuration : Parcelable {

    val shopperLocale: Locale
    val environment: Environment
    val clientKey: String
    val isAnalyticsEnabled: Boolean?
    val amount: Amount
}
