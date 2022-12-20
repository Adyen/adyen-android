package com.adyen.checkout.components.base

import android.os.Parcelable
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import java.util.Locale

interface Configuration : Parcelable {

    val shopperLocale: Locale
    val environment: Environment
    val clientKey: String
    val isAnalyticsEnabled: Boolean?
    val amount: Amount
}
