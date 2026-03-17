/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getBooleanOrNull
import com.adyen.checkout.core.common.internal.model.getIntOrNull
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
internal data class Brand(
    val brand: String? = null,
    val enableLuhnCheck: Boolean? = null,
    val supported: Boolean? = null,
    val cvcPolicy: String? = null,
    val expiryDatePolicy: String? = null,
    val panLength: Int? = null,
    val paymentMethodVariant: String? = null,
    val localizedBrand: String? = null,
) : ModelObject() {

    companion object {
        @Suppress("MemberNameEqualsClassName")
        private const val BRAND = "brand"
        private const val ENABLE_LUHN_CHECK = "enableLuhnCheck"
        private const val SUPPORTED = "supported"
        private const val CVC_POLICY = "cvcPolicy"
        private const val EXPIRY_DATE_POLICY = "expiryDatePolicy"
        private const val PAN_LENGTH = "panLength"
        private const val PAYMENT_METHOD_VARIANT = "paymentMethodVariant"
        private const val LOCALE_BRAND = "localeBrand"

        @JvmField
        val SERIALIZER: Serializer<Brand> = object : Serializer<Brand> {
            override fun serialize(modelObject: Brand): JSONObject {
                return JSONObject().apply {
                    putOpt(BRAND, modelObject.brand)
                    putOpt(ENABLE_LUHN_CHECK, modelObject.enableLuhnCheck)
                    putOpt(SUPPORTED, modelObject.supported)
                    putOpt(CVC_POLICY, modelObject.cvcPolicy)
                    putOpt(EXPIRY_DATE_POLICY, modelObject.expiryDatePolicy)
                    putOpt(PAN_LENGTH, modelObject.panLength)
                    putOpt(PAYMENT_METHOD_VARIANT, modelObject.paymentMethodVariant)
                    putOpt(LOCALE_BRAND, modelObject.localizedBrand)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Brand {
                return Brand(
                    brand = jsonObject.getStringOrNull(BRAND),
                    enableLuhnCheck = jsonObject.getBooleanOrNull(ENABLE_LUHN_CHECK),
                    supported = jsonObject.getBooleanOrNull(SUPPORTED),
                    cvcPolicy = jsonObject.getStringOrNull(CVC_POLICY),
                    expiryDatePolicy = jsonObject.getStringOrNull(EXPIRY_DATE_POLICY),
                    panLength = jsonObject.getIntOrNull(PAN_LENGTH),
                    paymentMethodVariant = jsonObject.getStringOrNull(PAYMENT_METHOD_VARIANT),
                    localizedBrand = jsonObject.getStringOrNull(LOCALE_BRAND)
                )
            }
        }
    }

    enum class FieldPolicy(val value: String) {
        REQUIRED("required"),
        OPTIONAL("optional"),
        HIDDEN("hidden");

        // We treat both HIDDEN and OPTIONAL the same way now, as optional, to avoid hiding and showing the cvc field
        // while the user is typing the card number
        fun isRequired(): Boolean {
            return this == REQUIRED
        }

        companion object {
            @JvmStatic
            fun parse(value: String): FieldPolicy {
                return when (value) {
                    REQUIRED.value -> REQUIRED
                    OPTIONAL.value -> OPTIONAL
                    HIDDEN.value -> HIDDEN
                    else -> throw IllegalArgumentException("No CvcPolicy matches the value of: $value")
                }
            }
        }
    }
}
