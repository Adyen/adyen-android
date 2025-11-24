/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/5/2025.
 */

package com.adyen.checkout.core.components.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.CheckoutCurrency
import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getLongOrNull
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

@Parcelize
data class Amount(
    // TODO - Check if we can make this non-nullable
    val currency: String? = null,
    val value: Long = 0L,
) : ModelObject() {

    companion object {
        private const val CURRENCY = "currency"
        private const val VALUE = "value"

        @JvmField
        val SERIALIZER: Serializer<Amount> = object : Serializer<Amount> {
            override fun serialize(modelObject: Amount): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(CURRENCY, modelObject.currency)
                        putOpt(VALUE, modelObject.value)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Amount::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Amount {
                return Amount(
                    currency = jsonObject.getStringOrNull(CURRENCY),
                    value = jsonObject.getLongOrNull(VALUE) ?: EMPTY_VALUE,
                )
            }
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Amount.format(locale: Locale): String {
    val currencyCode = currency
    val checkoutCurrency = CheckoutCurrency.find(currencyCode.orEmpty())
    val currency = Currency.getInstance(currencyCode)
    val currencyFormat = DecimalFormat.getCurrencyInstance(locale)
    currencyFormat.currency = currency
    val fractionDigits = checkoutCurrency?.fractionDigits ?: 0
    currencyFormat.minimumFractionDigits = fractionDigits
    currencyFormat.maximumFractionDigits = fractionDigits
    val value = BigDecimal.valueOf(value, fractionDigits)
    return currencyFormat.format(value)
}

// TODO - Originally in AmountExt
private const val EMPTY_VALUE = -1L
