/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/1/2021.
 */

package com.adyen.checkout.card.api.model

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getBooleanOrNull
import com.adyen.checkout.core.model.getIntOrNull
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class Brand(
    val brand: String? = null,
    val enableLuhnCheck: Boolean? = null,
    val supported: Boolean? = null,
    val cvcPolicy: String? = null,
    val expiryDatePolicy: String? = null,
    val panLength: Int? = null,
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        @Suppress("MemberNameEqualsClassName")
        private const val BRAND = "brand"
        private const val ENABLE_LUHN_CHECK = "enableLuhnCheck"
        private const val SUPPORTED = "supported"
        private const val CVC_POLICY = "cvcPolicy"
        private const val EXPIRY_DATE_POLICY = "expiryDatePolicy"
        private const val PAN_LENGTH = "panLength"

        @JvmField
        val CREATOR: Parcelable.Creator<Brand> = Creator(Brand::class.java)

        @JvmStatic
        val SERIALIZER: Serializer<Brand> = object : Serializer<Brand> {
            override fun serialize(modelObject: Brand): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(BRAND, modelObject.brand)
                    jsonObject.putOpt(ENABLE_LUHN_CHECK, modelObject.enableLuhnCheck)
                    jsonObject.putOpt(SUPPORTED, modelObject.supported)
                    jsonObject.putOpt(CVC_POLICY, modelObject.cvcPolicy)
                    jsonObject.putOpt(EXPIRY_DATE_POLICY, modelObject.expiryDatePolicy)
                    jsonObject.putOpt(PAN_LENGTH, modelObject.panLength)
                } catch (e: JSONException) {
                    throw ModelSerializationException(Brand::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): Brand {
                return try {
                    Brand(
                        brand = jsonObject.getStringOrNull(BRAND),
                        enableLuhnCheck = jsonObject.getBooleanOrNull(ENABLE_LUHN_CHECK),
                        supported = jsonObject.getBooleanOrNull(SUPPORTED),
                        cvcPolicy = jsonObject.getStringOrNull(CVC_POLICY),
                        expiryDatePolicy = jsonObject.getStringOrNull(EXPIRY_DATE_POLICY),
                        panLength = jsonObject.getIntOrNull(PAN_LENGTH),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(Brand::class.java, e)
                }
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
