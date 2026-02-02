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
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.CheckoutException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

@Parcelize
data class Amount(
    val currency: String,
    val value: Long = 0L,
) : ModelObject() {

    init {
        validateCurrency()
    }

    private fun validateCurrency() {
        if (!CheckoutCurrency.isSupported(currency)) {
            throw CheckoutException(
                CheckoutError(
                    code = CheckoutError.ErrorCode.INVALID_CURRENCY_CODE,
                    message = "Invalid currency code: $currency",
                ),
            )
        }
    }

    companion object {
        private const val CURRENCY = "currency"
        private const val VALUE = "value"

        @JvmField
        val SERIALIZER: Serializer<Amount> = object : Serializer<Amount> {
            override fun serialize(modelObject: Amount): JSONObject {
                return try {
                    JSONObject().apply {
                        put(CURRENCY, modelObject.currency)
                        put(VALUE, modelObject.value)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Amount::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Amount {
                return try {
                    Amount(
                        currency = jsonObject.getString(CURRENCY),
                        value = jsonObject.getLong(VALUE),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(Amount::class.java, e)
                }
            }
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Amount.format(locale: Locale): String {
    val currencyCode = currency
    val checkoutCurrency = CheckoutCurrency.find(currencyCode)
    val currency = Currency.getInstance(currencyCode)
    val currencyFormat = DecimalFormat.getCurrencyInstance(locale)
    currencyFormat.currency = currency
    val fractionDigits = checkoutCurrency?.fractionDigits ?: 0
    currencyFormat.minimumFractionDigits = fractionDigits
    currencyFormat.maximumFractionDigits = fractionDigits
    val value = BigDecimal.valueOf(value, fractionDigits)
    return currencyFormat.format(value)
}
